package service;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import site.fifa.FIFApp;
import site.fifa.entity.Country;
import site.fifa.repository.CountryRepository;
import site.fifa.service.CountryService;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FIFApp.class)
public class CountryServiceTest {

    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private CountryService countryService;

    @Test
    public void sampleTest() {
        Country ii = countryRepository.findByCountryName("ii");
        assertNull(ii);

        assertEquals(4L, countryService.getCountries().size());
        ii = countryRepository.findByCountryName("Ukraine");
        assertNotNull(ii);
    }

}
