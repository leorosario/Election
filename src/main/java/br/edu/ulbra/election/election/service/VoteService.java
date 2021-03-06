package br.edu.ulbra.election.election.service;

import br.edu.ulbra.election.election.client.CandidateClientService;
import br.edu.ulbra.election.election.client.VoterClientService;
import br.edu.ulbra.election.election.exception.GenericOutputException;
import br.edu.ulbra.election.election.input.v1.VoteInput;
import br.edu.ulbra.election.election.model.Election;
import br.edu.ulbra.election.election.model.Vote;
import br.edu.ulbra.election.election.output.v1.CandidateOutput;
import br.edu.ulbra.election.election.output.v1.GenericOutput;
import br.edu.ulbra.election.election.output.v1.VoterOutput;
import br.edu.ulbra.election.election.repository.ElectionRepository;
import br.edu.ulbra.election.election.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import feign.FeignException;

import java.util.List;

@Service
public class VoteService {

    private final VoteRepository voteRepository;
    private final VoterClientService voterClientService;
    private final ElectionRepository electionRepository;
    private final CandidateClientService candidateClientService;

    @Autowired
    public VoteService(VoteRepository voteRepository, ElectionRepository electionRepository, VoterClientService voterClientService, CandidateClientService candidateClientService) {
        this.voteRepository = voteRepository;
        this.electionRepository = electionRepository;
        this.voterClientService = voterClientService;
        this.candidateClientService = candidateClientService;

    }

    public GenericOutput electionVote(VoteInput voteInput, String token) {

        Election election = validateInput(voteInput.getElectionId(), voteInput);

        validateToken(token, voteInput.getVoterId());

        Vote vote = new Vote();
        vote.setElection(election);
        vote.setVoterId(voteInput.getVoterId());
        Long candidateId;

        if (voteInput.getCandidateNumber() == null) {
            vote.setBlankVote(true);
        } else {
            vote.setBlankVote(false);
        }

        // TODO: Validate null candidate
        candidateId = checkNumberCandidates(voteInput.getCandidateNumber());
        if(candidateId == null){
            vote.setNullVote(true);
        }else{
            vote.setCandidateId(candidateId);
            vote.setNullVote(false);
        }

        voteRepository.save(vote);

        return new GenericOutput("OK");
    }

    public GenericOutput multiple(List<VoteInput> voteInputList, String token) {
        for (VoteInput voteInput : voteInputList) {
            this.electionVote(voteInput, token);
        }
        return new GenericOutput("OK");
    }

    public Boolean getVoteByVoterId(Long id) {
        List<Vote> votes = voteRepository.findByVoterId(id);
        if (votes.size() > 0) {
            return true;
        }
        return false;
    }

    private void validateToken(String token, Long voterId){
        try {
            VoterOutput voterOutput = voterClientService.checkToken(token);
            if(voterOutput == null){
                throw new GenericOutputException("Invalid token");
            }

            if(!voterOutput.getId().equals(voterId)){
                throw new GenericOutputException("Invalid token");
            }
        } catch (FeignException e) {
            if (e.status() == 500) {
                throw new GenericOutputException("Invalid token");
            }
        }
    }

    private Long checkNumberCandidates(Long number) {
        Long id = null;
        int i;
        try {
            List<CandidateOutput> candidates = candidateClientService.getAll();
            CandidateOutput candidate;

            for (i = 0; i < candidates.size(); i++) {
                candidate = candidates.get(i);
                if (candidate.getNumberElection().equals(number)) {
                    id = candidate.getId();
                    return id;
                }
            }
        } catch (FeignException e) {
            if (e.status() == 500) {
                throw new GenericOutputException("Invalid candidate");
            }
        }
        return (id);
    }

    public Election validateInput(Long electionId, VoteInput voteInput) {
        Election election = electionRepository.findById(electionId).orElse(null);
        if (election == null) {
            throw new GenericOutputException("Invalid Election");
        }
        if (voteInput.getVoterId() == null) {
            throw new GenericOutputException("Invalid Voter");
        }

        try {
            VoterOutput voterOutput = voterClientService.getById(voteInput.getVoterId());
            if (voterOutput == null) {
                throw new GenericOutputException("Invalid Voter");
            }
        } catch (FeignException e) {
            if (e.status() == 500) {
                throw new GenericOutputException("Invalid Voter");
            }
        }

        List<Vote> votes = voteRepository.findByVoterIdAndElection_Id(voteInput.getVoterId(), voteInput.getElectionId());
        if (votes.size() > 0) {
            throw new GenericOutputException("This voter already voted in this election.");
        }

        // TODO: Validate voter

        return election;
    }
}
