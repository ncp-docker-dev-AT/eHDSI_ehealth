package epsos.ccd.gnomon.configmanager;

@Deprecated
public class PropertySearchableContainer {
	private String value;
	private boolean isSearchable;
        
        public PropertySearchableContainer() {}
        
        public PropertySearchableContainer(String value, boolean isSearchable) {
            this.value = value;
            this.isSearchable = isSearchable;
        }
        
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public boolean isSearchable() {
		return isSearchable;
	}
	public void setSearchable(boolean isSearchable) {
		this.isSearchable = isSearchable;
	}
	
}
