package br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType;

public class NetworkProtocol {

	private Integer id;
	

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || ! (obj instanceof UpNetworkInterface) ){
			return false;
		}
		
		NetworkProtocol d = (NetworkProtocol) obj;
		
		return this.id == d.id || this.id.equals(d.id);
	}
}
