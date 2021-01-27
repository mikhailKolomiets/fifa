package site.fifa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.fifa.entity.Stadium;
import site.fifa.entity.StadiumTypeEnum;
import site.fifa.repository.StadiumRepository;

@Service
public class StadiumService {

    @Autowired
    private StadiumRepository stadiumRepository;

    public Stadium createDefaultStadium() {
        Stadium stadium = new Stadium();
        stadium.setType(StadiumTypeEnum.VILLAGE);
        stadium.setPopulation(stadium.getType().getPopulation());
        return stadiumRepository.save(stadium);
    }

    public Stadium getById(Long id) {
        return stadiumRepository.findById(id).orElse(null);
    }

    public Stadium changeTicketPrice(int price, Long stadiumId) {
        Stadium stadium = getById(stadiumId);
        if (stadium == null) {
            return null;
        }
        stadium.setTicketPrice(price);
        stadiumRepository.save(stadium);
        return stadium;
    }

}
