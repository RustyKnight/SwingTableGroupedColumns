/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testgroupedcolumns;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 *
 * @author shane
 */
public class TestGroups {

	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
				}

				JTable table = new JTable();
				table.setAutoCreateColumnsFromModel(false);
				table.setShowGrid(true);
				table.setGridColor(Color.GRAY);

				DefaultTableModel tm = new DefaultTableModel(0, 10);

				for (int index = 0; index < 10; index++) {
					Object[] row = new Object[10];
					for (int col = 0; col < 10; col++) {
						row[col] = index + "x" + col;
					}
					tm.addRow(row);
				}
				table.setModel(tm);

				GroupableColumnModel model = new GroupableColumnModel();
				model.addColumn(createColumn("A", 0));
				model.addColumn(createColumn("B", 1));
				model.addColumn(createColumn("C", 2));
				model.addColumn(createColumn("D", 3));
				model.addColumn(createColumn("E", 4));
				model.addColumn(createColumn("F", 5));
				model.addColumn(createColumn("G", 6));
				model.addColumn(createColumn("H", 7));
				model.addColumn(createColumn("I", 8));
				model.addColumn(createColumn("J", 9));

				GroupableColumnModel.IColumnGroup groupA = model.addGroup("Test 01");
				groupA.addColumn(model.getColumn(model.getColumnIndex("A")));
				groupA.addColumn(model.getColumn(model.getColumnIndex("B")));
				groupA.addColumn(model.getColumn(model.getColumnIndex("C")));
				groupA.addColumn(model.getColumn(model.getColumnIndex("D")));

				GroupableColumnModel.IColumnGroup groupB = model.addGroup("Test 02");
				groupB.addColumn(model.getColumn(model.getColumnIndex("F")));
				groupB.addColumn(model.getColumn(model.getColumnIndex("G")));
				groupB.addColumn(model.getColumn(model.getColumnIndex("H")));
				groupB.addColumn(model.getColumn(model.getColumnIndex("I")));

				table.setColumnModel(model);
				table.setTableHeader(new GroupableTableHeader(table.getColumnModel()));

				table.setAutoCreateRowSorter(true);

				JFrame frame = new JFrame("Testing");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setLayout(new BorderLayout());
				frame.add(new JScrollPane(table));
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});

	}

	protected static TableColumn createColumn(String title, int modelIndex) {
		TableColumn column = new TableColumn();
		column.setHeaderValue(title);
		column.setIdentifier(title);
		column.setModelIndex(modelIndex);
		return column;
	}
}
