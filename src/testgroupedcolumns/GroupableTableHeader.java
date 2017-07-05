/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testgroupedcolumns;

import javax.swing.plaf.TableHeaderUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import testgroupedcolumns.GroupableColumnModel.IColumnGroup;

/**
 *
 * @author shane
 */
public class GroupableTableHeader extends JTableHeader {

	private IColumnGroup draggedGroup;
	
	public GroupableTableHeader(TableColumnModel model) {
		super(model);
		super.setUI(new GroupableTableHeaderUI());
		setReorderingAllowed(true);
	}

	@Override
	public void setUI(TableHeaderUI ui) {
	}

	public void setDraggedGroup(IColumnGroup columnGroup) {
		
		draggedGroup = columnGroup;
		
	}

	public IColumnGroup getDraggedGroup() {

		return draggedGroup;
		
	}
	
}
