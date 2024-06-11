package br.com.alura.screenmatch.Service;

import br.com.alura.screenmatch.model.DadosSerie;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConverterDados implements IConverteDados{
    private ObjectMapper mapper = new ObjectMapper();


    @Override
    public <T> T obterDados(String json, Class<T> classe) {
        try {
            return mapper.readValue(json, classe); //converter qualquer classe que foi passado
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
