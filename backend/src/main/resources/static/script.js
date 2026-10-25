async function carregarClima() {
    const conteudo = document.getElementById('conteudo');
    conteudo.innerHTML = '<p class="loading">Obtendo sua localização...</p>';

    if (!navigator.geolocation) {
        conteudo.innerHTML = '<p class="erro">Geolocalização não suportada neste navegador.</p>';
        return;
    }

        navigator.geolocation.getCurrentPosition(async (position) => {
            const lat = position.coords.latitude;
            const lon = position.coords.longitude;

            try {
            const response = await fetch(`/api/temp?lat=${lat}&lon=${lon}`);
            
            if (!response.ok) {
                throw new Error('Erro ao buscar dados do clima');
            }

            const data = await response.json();

            conteudo.innerHTML = `
                <h1>Clima Atual</h1>
                <p class="cidade">${data.cidade || 'Localização atual'}</p>
                <div class="temp">${Math.round(data.temperatura)}°</div>
                <p class="descricao">${data.descricao}</p>
                
                <div class="info">
                    <div class="info-item">
                        <span>Sensação</span>
                        <span>${Math.round(data.sensacaoTermica)}°</span>
                    </div>
                    <div class="info-item">
                        <span>Umidade</span>
                        <span>${data.umidade}%</span>
                    </div>
                </div>
            `;
        } catch (error) {
            conteudo.innerHTML = `
                <p class="erro">Não foi possível carregar o clima.</p>
                <p style="margin-top:10px; font-size:0.9rem; opacity:0.8">
                    Verifique se a API está rodando em http://localhost:8080
                </p>
            `;
            console.error(error);
        }
    }, (error) => {
        conteudo.innerHTML = `
            <p class="erro">Permissão de localização negada.</p>
            <p style="margin-top:10px; font-size:0.9rem; opacity:0.8">
                Permita o acesso à localização e tente novamente.
            </p>
        `;
    });
}

// Carrega automaticamente ao abrir a página
carregarClima();