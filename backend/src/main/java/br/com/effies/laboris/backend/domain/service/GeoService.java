package br.com.effies.laboris.backend.domain.service;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeoService {

    private final GeoApiContext context;

    public GeoService(@Value("${google.maps.api.key}") String apiKey){
        this.context = new GeoApiContext.Builder()
            .apiKey(apiKey)
            .build();
    }

    public LatLng geocodeAddress(String address){
        try {
            GeocodingResult[] results = GeocodingApi.geocode(context, address).await();

            if(results != null && results.length > 0){
                return  results[0].geometry.location;
            }
        } catch (Exception e){
            System.err.println("Erro ao fazer geocode do endereço: " + e.getMessage());
        }
        return  null;
    }
}
