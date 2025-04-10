package br.com.picpay_desafio_backend.authorization;

import br.com.picpay_desafio_backend.model.Data;

public record Authorization(
        String status,
        Data data
) {
    public boolean isAuthorized() {
        return "success".equals(status) && data != null && data.authorization();
    }
}
