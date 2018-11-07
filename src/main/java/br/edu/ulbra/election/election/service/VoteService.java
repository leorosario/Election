package br.edu.ulbra.election.election.service;

import br.edu.ulbra.election.election.exception.GenericOutputException;
import br.edu.ulbra.election.election.input.v1.VoteInput;
import br.edu.ulbra.election.election.model.Vote;
import br.edu.ulbra.election.election.output.v1.GenericOutput;
import br.edu.ulbra.election.election.repository.VoteRepository;
import org.apache.commons.lang.StringUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.List;

@Service
public class VoteService {

    private final VoteRepository voteRepository;

    private final ModelMapper modelMapper;

    private static final String MESSAGE_INVALID_ID = "Invalid id";
    private static final String MESSAGE_VOTE_NOT_FOUND = "Vote not found";

    @Autowired
    public VoteService(VoteRepository voteRepository, ModelMapper modelMapper) {
        this.voteRepository = voteRepository;
        this.modelMapper = modelMapper;
    }

    public GenericOutput create(VoteInput voteInput) {
        validateInput(voteInput);
        modelMapper.getConfiguration().setAmbiguityIgnored(true);
        Vote vote = modelMapper.map(voteInput, Vote.class);
        vote = voteRepository.save(vote);
        return new GenericOutput("ta certo");
    }



    public void validateInput(VoteInput voteInput) {
        if (voteInput.getCandidateId() == null) {
            throw new GenericOutputException("Invalid CandidateId");
        }

        if (voteInput.getElectionId() == null) {
            throw new GenericOutputException("Invalid ElectionId");
        }

        if (voteInput.getVoterId() == null) {
            throw new GenericOutputException("Invalid Voter");
        }
    }

}

