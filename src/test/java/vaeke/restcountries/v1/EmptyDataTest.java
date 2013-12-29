package vaeke.restcountries.v1;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import vaeke.restcountries.v1.domain.Country;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class EmptyDataTest {
	
List<Country> countries;
	
	@Before
	public void before() throws IOException {
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("countriesV1.json");
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new InputStreamReader(is, "UTF-8"));
		countries = new ArrayList<Country>();
		reader.beginArray();
		while(reader.hasNext()) {
			Country country = gson.fromJson(reader, Country.class);
			countries.add(country);
		}
		reader.endArray();
        reader.close();
	}
	
	@Test
	public void emptyPopulation() {
		List<Country> result = new ArrayList<Country>();
		for(Country c : countries) {
			if(c.getPopulation() == null) {
				result.add(c);
			}
		}
		System.out.println("Empty Population\n");
		for(Country c : result) {
			System.out.println(c.getName());
		}
	}
	
	@Test
	public void emptyLanguageCodes() throws Exception {
		List<Country> result = new ArrayList<Country>();
		for(Country c : countries) {
			result.add(c);
		}
		System.out.println("Empty Language Codes\n");
		for(Country c : result) {
			if(c.getLanguagesCodes() == null || c.getLanguagesCodes().isEmpty())
				System.out.println(c.getName());
		}
	}

}