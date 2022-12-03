package com.autobots.automanager.hateoas;

import java.util.List;

public interface AdicionadorLink<T>{
	
	public void AdicionarLink(List <T> link);
	
	public void AdicionarLink(T link);

}
