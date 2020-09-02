package site.fifa.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import site.fifa.entity.Country;

@Repository
public interface CountryRepository extends CrudRepository<Country, Long> {
    Country findByCountryName(String countryName);
}
