package br.edu.ulbra.election.election.service;


import br.edu.ulbra.election.election.client.CandidateClientService;
import br.edu.ulbra.election.election.client.VoterClientService;
import br.edu.ulbra.election.election.exception.GenericOutputException;
import br.edu.ulbra.election.election.input.v1.VoteInput;
import br.edu.ulbra.election.election.model.Election;
import br.edu.ulbra.election.election.model.Vote;
import br.edu.ulbra.election.election.output.v1.CandidateOutput;
import br.edu.ulbra.election.election.output.v1.ElectionCandidateResultOutput;
import br.edu.ulbra.election.election.output.v1.GenericOutput;
import br.edu.ulbra.election.election.output.v1.VoterOutput;
import br.edu.ulbra.election.election.repository.ElectionRepository;
import br.edu.ulbra.election.election.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import feign.FeignException;

import java.util.List;

@Service
public class ResultService {
    private final VoteRepository voteRepository;
    private final ElectionRepository electionRepository;
    private final CandidateClientService candidateClientService;

    @Autowired
    public ResultService(VoteRepository voteRepository, ElectionRepository electionRepository, CandidateClientService candidateClientService) {
        this.voteRepository = voteRepository;
        this.electionRepository = electionRepository;
        this.candidateClientService = candidateClientService;

    }

    public ElectionCandidateResultOutput getById(Long candidateId){
        ElectionCandidateResultOutput electionCandidateResultOutput = new ElectionCandidateResultOutput();
        long totalVotes;
        try {
            electionCandidateResultOutput.setCandidate(candidateClientService.getById(candidateId));
        } catch (FeignException e) {
            if (e.status() == 500) {
                throw new GenericOutputException("Invalid Candidate");
            }
        }


        totalVotes = voteRepository.findByCandidateId(candidateId).size();
        electionCandidateResultOutput.setTotalVotes(totalVotes);

        return electionCandidateResultOutput;
    }
}
