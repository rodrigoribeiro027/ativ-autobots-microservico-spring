package com.autobots.automanager.filtragens;

class AnalisadorCabecalho {
	private String cabecalho;

	public AnalisadorCabecalho(String cabecalho) {
		this.cabecalho = cabecalho;
	}

	public String obterJwt() {
		String[] partes = cabecalho.split(" ");
		String jwt = partes[1];
		return jwt;
	}
}