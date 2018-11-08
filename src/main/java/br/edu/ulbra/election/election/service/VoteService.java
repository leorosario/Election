package br.edu.ulbra.election.election.service;

import br.edu.ulbra.election.election.exception.GenericOutputException;
import br.edu.ulbra.election.election.input.v1.VoteInput;
import br.edu.ulbra.election.election.model.Vote;
import br.edu.ulbra.election.election.output.v1.GenericOutput;
import br.edu.ulbra.election.election.repository.VoteRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VoteService {

    private final VoteRepository voteRepository;

    private final ModelMapper modelMapper;

    @Autowired
    public VoteService(VoteRepository voteRepository, ModelMapper modelMapper) {
        this.voteRepository = voteRepository;
        this.modelMapper = modelMapper;
    }

    public GenericOutput electionVote(VoteInput voteInput) {
        validateInput(voteInput);
        modelMapper.getConfiguration().setAmbiguityIgnored(true);
        Vote vote = modelMapper.map(voteInput, Vote.class);
        vote = voteRepository.save(vote);
        return new GenericOutput("ok");
    }

    public void validateInput(VoteInput voteInput) {
        List<Vote> votes;
        if (voteInput.getCandidateNumber() == null) {
            throw new GenericOutputException("Invalid CandidateNumber");
        }

        if (voteInput.getElectionId() == null) {
            throw new GenericOutputException("Invalid ElectionId");
        }

        if (voteInput.getVoterId() == null) {
            throw new GenericOutputException("Invalid Voter");
        }

        votes = voteRepository.findByVoterIdAndElectionId(voteInput.getVoterId(), voteInput.getElectionId());

        if(votes.size() > 0){
            throw new GenericOutputException("This voter already voted in this election");
        }
    }

}

