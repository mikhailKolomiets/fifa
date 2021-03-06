package site.fifa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.fifa.entity.WIKI;
import site.fifa.repository.WIKIRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WIKIService {

    @Autowired
    private WIKIRepository wikiRepository;

    public WIKI createWiki(WIKI wiki) {
        return wikiRepository.save(wiki);
    }

    public WIKI editWiki(WIKI wiki) {
        WIKI result = wikiRepository.findById(wiki.getId()).orElse(null);
        if (result == null) {
            return wiki;
        }
        if (wiki.getName() != null) {
            result.setName(wiki.getName());
        }
        if (wiki.getDescription() != null) {
            result.setDescription(wiki.getDescription());
        }
        result.setModify(LocalDateTime.now());
        return wikiRepository.save(result);
    }

    public List<WIKI> getAllWikis() {
        return wikiRepository.findAll();
    }

}
