- Ollama -> LLAVA - serve para comandar instruções contendo imagens
	- em vem de ollama temos LMDeploy
		- **LLaVA** usa um Vision Transformer + LLM

-  **CLIP** alinham texto e imagem
- **Fuyu** não tem encoder, lê pixels diretamente
- **GPT‑4V** faz análise profunda de imagens
    
- **Whisper** para transcrever áudio
		→ Se um professor disser “Estou livre terça das 14h às 18h”, o Whisper transcreve e o LLM converte para JSON.

**Aplicação para projeto:** → Para interpretar fotos de horários, PDFs ou imagens enviadas pelos alunos, o modelo ideal é **LLaVA** (open‑source) ou **GPT‑4V**.

--------------
##  Como tudo encaixar no sistema de horários:
	1) Input do utilizador
		- áudio
		- imagem
		- PDF
		- texto informal
		
	2) Processamento multimodal
		- Whisper → áudio → texto
		- LLaVA → imagem/PDF → texto estruturado
		- LLM (Gemma/Mistral/Phi)→ texto informal → JSON
    
	3) Normalização
		- Transformar tudo em blocos uniformes (1h, 30min, etc.).

	4) Envio para o backend
		- O backend recebe sempre algo assim:

json
```
{
  "dia": "terça-feira",
  "inicio": "14:00",
  "fim": "18:00"
}
```

---------------------------------
# links/videos:

https://youtu.be/-m4n3lsCtcA?si=U3f46thWDF-x5g9L

https://www.youtube.com/watch?v=MFZYWufjxyA

https://www.youtube.com/watch?v=_sGwL6RAsUc

https://www.youtube.com/watch?v=28lC4fqukoc

https://hub.asimov.academy/tutorial/llms-multimodais/?utm_source=copilot.com

https://blogs.novita.ai/pt/large-multimodal-models-lmms-a-gigantic-leap-in-ai-world/?utm_source=copilot.com

https://www.linkedin.com/pulse/llms-e-multimodalidade-integra%C3%A7%C3%A3o-de-texto-imagem-%C3%A1udio-osmundo-oapkf/

