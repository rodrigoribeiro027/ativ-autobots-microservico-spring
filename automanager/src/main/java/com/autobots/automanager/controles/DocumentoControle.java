package com.autobots.automanager.controles;
import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.componentes.DocumentoAtualizador;
import com.autobots.automanager.componentes.UsuarioSelecionador;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.hateoas.AdicionadorLinkUsuario;
import com.autobots.automanager.repositorios.RepositorioDocumento;
import com.autobots.automanager.repositorios.RepositorioUsuario;

@RestController

@RequestMapping("/documento")

public class DocumentoControle {
	
	@Autowired
	private RepositorioUsuario repositorioUsu;
	@Autowired
	private RepositorioDocumento repositorioDoc;
	@Autowired
	private AdicionadorLinkUsuario usuarioLink;
	@Autowired 
	private UsuarioSelecionador usuarioSelecionador;
	
	@GetMapping("/BuscarDocumentos")
	public ResponseEntity<List<Documento>> BuscarDocumentos() {
		List<Documento> documentos = repositorioDoc.findAll();

		if (documentos.isEmpty()) {
			ResponseEntity<List<Documento>> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} 
		else {
			ResponseEntity<List<Documento>> resposta = new ResponseEntity<>(documentos, HttpStatus.FOUND);
			return resposta;
		}
	}

	@GetMapping("/documentos/{usuarioId}")
	public ResponseEntity<?> BuscardocumentosClienteID(@PathVariable Long usuarioId){
		List <Usuario> usuarios = repositorioUsu.findAll();
		Usuario alvo = usuarioSelecionador.selecionar(usuarios, usuarioId);
		if (alvo == null) {
			ResponseEntity< List<Documento>> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} 
		else {
			usuarioLink.AdicionarLink(alvo);
			return new ResponseEntity<>(alvo.getDocumentos(), HttpStatus.FOUND);	
		}

	}
	
	@PutMapping("/cadastrar") 
	public ResponseEntity<?> cadastrarDocumento(@RequestBody Usuario atualizacao) {
		HttpStatus status = HttpStatus.CONFLICT;

		Usuario alvo = repositorioUsu.getById(atualizacao.getId());

		DocumentoAtualizador atualizador = new DocumentoAtualizador();

		if (alvo != null) {
			alvo.getDocumentos().addAll(atualizacao.getDocumentos());
			repositorioUsu.save(alvo);
			status = HttpStatus.OK;
		} 
		else {
			status = HttpStatus.BAD_REQUEST;
		}
		return new ResponseEntity<>(status);
	}
	
	@PutMapping("/documentos/atualizar/{usuarioId}")
	public ResponseEntity<?> atualizarDocumento(@RequestBody Usuario atualizacao, @PathVariable Long usuarioId) {
		List <Usuario> usuarios = repositorioUsu.findAll();
		Usuario alvo = usuarioSelecionador.selecionar(usuarios, usuarioId);
		if (alvo == null) {
			ResponseEntity< List<Documento>> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} 
		else {
			usuarioLink.AdicionarLink(alvo);
			alvo.getDocumentos().addAll(atualizacao.getDocumentos());
			repositorioUsu.save(alvo);
			ResponseEntity<List<Documento>> resposta = new ResponseEntity <>(HttpStatus.FOUND);
			return new ResponseEntity<>(alvo.getDocumentos(), HttpStatus.FOUND);
		}
	}
	
	@PutMapping("/atualizarDocCadastrado/{usuarioId}")
	public ResponseEntity<?> atualizarDocCadastrado (@RequestBody Documento atualizacao, @PathVariable Long usuarioId) {
		List <Usuario> usuarios = repositorioUsu.findAll();
		Usuario alvo = usuarioSelecionador.selecionar(usuarios, usuarioId);
		DocumentoAtualizador attDoc = new DocumentoAtualizador();
		
		if (alvo == null) {
			ResponseEntity< List<Documento>> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
			return resposta;
		} 
		else {
			usuarioLink.AdicionarLink(alvo);
			for (Documento docs : alvo.getDocumentos() ) {
				if (docs.getId() == atualizacao.getId()) {
					Documento getId = repositorioDoc.getById(atualizacao.getId()); 
					attDoc.atualizar(getId, atualizacao);
					repositorioDoc.save(getId);
				}
			}
			repositorioUsu.save(alvo);
			ResponseEntity<List<Documento>> resposta = new ResponseEntity <>(HttpStatus.FOUND);
			return resposta;	
		}		
	}
	
	@DeleteMapping("doc/excluir/{idDocumento}")
	public ResponseEntity<?> excluirDocumento(@PathVariable Long idDocumento){
		Documento verificacao = repositorioDoc.findById(idDocumento).orElse(null);
		
		if(verificacao == null) {
			return new ResponseEntity<>("Documento não econtrado", HttpStatus.NOT_FOUND);
		}
		else {
			for(Usuario usuario: repositorioUsu.findAll()) {
				if(!usuario.getDocumentos().isEmpty()) {
					for(Documento documento: usuario.getDocumentos()) {
						if(documento.getId() == idDocumento) {
							usuario.getDocumentos().remove(documento);
							repositorioUsu.save(usuario);
						}
						break;
					}
				}
			}
			
			return new ResponseEntity<>("Documento excluído :D", HttpStatus.ACCEPTED);
		}
	}
}