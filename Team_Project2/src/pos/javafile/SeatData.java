package pos.javafile;

import java.io.Serializable;
import java.util.List;

import data.TableData;

public class SeatData implements Serializable{

	int col;
	int row;
	List<TableData> tables;
	
	public int getCol() {
		return col;
	}
	public void setCol(int col) {
		this.col = col;
	}
	public int getRow() {
		return row;
	}
	public void setRow(int row) {
		this.row = row;
	}
	public List<TableData> getTables() {
		return tables;
	}
	public void setTables(List<TableData> tables) {
		this.tables = tables;
	}
	
	
}
