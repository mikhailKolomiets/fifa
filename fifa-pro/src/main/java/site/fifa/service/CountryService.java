package site.fifa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.fifa.entity.Country;
import site.fifa.repository.CountryRepository;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepository countryRepository;
    /**
     *  add country to the database only from this code place
     */
    private final List<Country> stubCountries = Arrays.asList(
        new Country(1L, "Ukraine", 1),
        new Country(2L, "Germany", 1),
        new Country(3L, "France", 1),
            new Country(null, "Spain", 15)
    );

    public List<Country> getCountries() {
        List<Country> countries = (List<Country>) countryRepository.findAll();

        if (countries.size() < stubCountries.size()) {
            for (int i = countries.size(); i < stubCountries.size(); i++ ) {
                countries.add(countryRepository.save(stubCountries.get(i)));
            }
        }

        return countries;
    }
}
