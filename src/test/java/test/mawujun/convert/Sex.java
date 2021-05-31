package test.mawujun.convert;

public enum Sex {
Man("男"),Women("女");
	
	private String name;
	Sex(String name ){
		this.name=name;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
