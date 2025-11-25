package ec.edu.upse.backend.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ec.edu.upse.backend.Domain.ChannelValidator;
import ec.edu.upse.backend.Entity.ChannelEntity;
import ec.edu.upse.backend.Repository.ChannelRepository;

@Service
public class ChannelService {
    @Autowired
    private ChannelRepository channelRepository;

    // CREATE
    public ChannelEntity save(ChannelEntity channel) {
        String normalizedName = ChannelValidator.normalizarNombre(channel.getName());
        if (normalizedName == null) {
            throw new IllegalArgumentException("Nombre de canal inválido");
        }

        channel.setName(normalizedName);

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

            String normalizedName = ChannelValidator.normalizarNombre(newData.getName());
            if (normalizedName == null) {
                throw new IllegalArgumentException("Nombre de canal inválido");
            }

            channel.setName(normalizedName);
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

    public List<ChannelEntity> searchPublic(String name) {
        return channelRepository
            .findByTypeAndNameContainingIgnoreCase("PUBLIC", name);
    }


    public ChannelEntity joinChannel(String channelId, String userId) {
        Optional<ChannelEntity> opt = channelRepository.findById(channelId);
        if (!opt.isPresent()) return null;

        ChannelEntity ch = opt.get();

        if (!"PUBLIC".equals(ch.getType())) {
            throw new IllegalArgumentException("Solo puedes unirte a canales PUBLIC");
        }

        if (!ch.getMembers().contains(userId)) {
            ch.getMembers().add(userId);
        }

        return channelRepository.save(ch);
    }
    // Filtrado por miembro
    public List<ChannelEntity> getChannelsByMemberId(String userId) {
        // Llama al nuevo método del Repository para filtrar por ID de miembro
        return channelRepository.findByMembersContaining(userId); 
    }
}