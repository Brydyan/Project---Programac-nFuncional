package ec.edu.upse.backend.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ec.edu.upse.backend.Entity.ChannelEntity;
import ec.edu.upse.backend.Repository.ChannelRepository;

@Service
public class ChannelService {
    @Autowired
    private ChannelRepository channelRepository;

    // CREATE
    public ChannelEntity save(ChannelEntity channel) {
        return channelRepository.save(channel);
    }

    // READ
    public List<ChannelEntity> getAllChannels() {
        return channelRepository.findAll();
    }

    public Optional<ChannelEntity> getChannelById(String id) {
        return channelRepository.findById(id);
    }

    // UPDATE
    public ChannelEntity updateChannel(String id, ChannelEntity newData) {
        Optional<ChannelEntity> aux = channelRepository.findById(id);
        if (aux.isPresent()) {
            ChannelEntity channel = aux.get();
            channel.setName(newData.getName());
            channel.setType(newData.getType());
            channel.setMembers(newData.getMembers());
            return channelRepository.save(channel);
        } else {
            return null;
        }
    }

    // DELETE
    public boolean deleteChannel(String id) {
        if (channelRepository.existsById(id)) {
            channelRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
