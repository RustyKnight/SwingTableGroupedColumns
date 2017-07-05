/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testgroupedcolumns;

import javax.swing.table.TableColumn;
import testgroupedcolumns.GroupableColumnModel.IColumnGroup;

/**
 *
 * @author shane
 */
public class Test {
	
	public static void main(String[] args) {
		
		GroupableColumnModel model = new GroupableColumnModel();
		model.addColumn(createColumn("A"));
		model.addColumn(createColumn("B"));
		model.addColumn(createColumn("C"));
		model.addColumn(createColumn("D"));
		model.addColumn(createColumn("E"));
		model.addColumn(createColumn("F"));
		model.addColumn(createColumn("G"));
		model.addColumn(createColumn("H"));
		model.addColumn(createColumn("I"));
		model.addColumn(createColumn("J"));
		
		System.out.println("--------------------------------------------------------------------------------");
		dump(model);
		
		IColumnGroup groupA = model.addGroup("Test 01");
		groupA.addColumn(model.getColumn(model.getColumnIndex("A")));
		groupA.addColumn(model.getColumn(model.getColumnIndex("C")));
		groupA.addColumn(model.getColumn(model.getColumnIndex("D")));
		groupA.addColumn(model.getColumn(model.getColumnIndex("J")));

		IColumnGroup groupB = model.addGroup("Test 02");
		groupB.addColumn(model.getColumn(model.getColumnIndex("B")));
		groupB.addColumn(model.getColumn(model.getColumnIndex("I")));
		groupB.addColumn(model.getColumn(model.getColumnIndex("H")));
		groupB.addColumn(model.getColumn(model.getColumnIndex("F")));

		System.out.println("--------------------------------------------------------------------------------");
		dump(model);

		groupA.removeColumn(model.getColumn(model.getColumnIndex("D")));

		System.out.println("--------------------------------------------------------------------------------");
		dump(model);
		
	}

	private static TableColumn createColumn(String title) {
		TableColumn column = new TableColumn();
		column.setHeaderValue(title);
		column.setIdentifier(title);
		return column;
	}

	private static void dump(GroupableColumnModel model) {
		for (int index = 0; index < model.getColumnCount(); index++) {
			System.out.println(model.getColumn(index).getHeaderValue());
		}
	}
	
}
