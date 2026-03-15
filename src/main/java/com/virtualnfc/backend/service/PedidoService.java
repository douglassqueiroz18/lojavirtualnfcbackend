package com.virtualnfc.backend.service;

import com.virtualnfc.backend.dto.ItemPedidoDto;
import com.virtualnfc.backend.dto.PedidoDto;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class PedidoService {

    private final String PAGBANK_URL = "https://sandbox.api.pagseguro.com/checkouts";
    private final String TOKEN = "060909b0-be80-492a-9ac4-059a44184d86eb0ad1d04fa5952c81cb3827712bd6d390a6-43fd-4d0b-a40e-de3494b12267";

    public String gerarLinkPagamento(PedidoDto pedidoDto) {
        List<Map<String, Object>> itensValidados = new ArrayList<>();

        for (ItemPedidoDto itemEnviado : pedidoDto.itens()) {

            double precoSeguro = itemEnviado.preco();

            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("name", itemEnviado.nome());
            itemMap.put("quantity", itemEnviado.quantidade());
            itemMap.put("unit_amount", (int) (precoSeguro * 100));
            itensValidados.add(itemMap);
        }

        return chamarApiPagBank(itensValidados);
    }

    private String chamarApiPagBank(List<Map<String, Object>> itens) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(TOKEN);

        Map<String, Object> body = new HashMap<>();
        body.put("reference_id", "PEDIDO-" + System.currentTimeMillis());
        body.put("items", itens);
        body.put("redirect_url", "https://example.com/sucesso");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(PAGBANK_URL, request, Map.class);
            List<Map<String, String>> links = (List<Map<String, String>>) response.getBody().get("links");
            
            return links.stream()
                    .filter(l -> "PAY".equals(l.get("rel")))
                    .findFirst()
                    .map(l -> l.get("href"))
                    .orElseThrow(() -> new RuntimeException("Link não gerado"));
        } catch (Exception e) {
            throw new RuntimeException("Erro na API PagBank: " + e.getMessage());
        }
    }
}