import { importLibrary } from "@googlemaps/js-api-loader";
import React, {useRef, useEffect} from "react";

const PlacesAutocompleteInput = ({ onPlaceSelect, value }) => {

    const containerRef = useRef(null);
    const autocompleteRef = useRef(null);

    useEffect(()=>{

        const initAutocomplete = async ()=>{
        
            try{

                const { PlaceAutocompleteElement } = await importLibrary('places');

                if(containerRef.current && !autocompleteRef.current){
                    const autocompleteElement = new PlaceAutocompleteElement({
                        requestedLanguage: 'pt-BR',
                        requestedRegion: 'BR',
                        types: ['address']
                    });

                    containerRef.current.appendChild(autocompleteElement)

                    autocompleteElement.addEventListener('gmp-select', async ({placePrediction}) => {
                        const place = placePrediction.toPlace();
                        await place.fetchFields({fields: ['formattedAddress', 'location']})
                        if(place && place.formattedAddress){
                            const lat = place.location ? place.location.lat() : null;
                            const lng = place.location ? place.location.lng() : null;
                            onPlaceSelect(place.formattedAddress, lat, lng)
                        }
                    });

                    autocompleteRef.current = autocompleteElement;
                }
            } catch(e){
                console.error("Erro ao importar a biblioteca 'places':", e)
            }
        };

        initAutocomplete();

    }, [onPlaceSelect]);

    useEffect(()=>{
        if(autocompleteRef.current && autocompleteRef.current.input.value !== value){
            autocompleteRef.current.input.value = value || '';
        }
    }, [value]);

    return (
        <div ref={containerRef} style={{ width: '300px' }} />
    )
}

export default PlacesAutocompleteInput;