package br.com.alura.screenmatch.Service;

public interface IConverteDados {
    <T> T obterDados(String json, Class<T> classe); //o T torna genérico o que espera
}
