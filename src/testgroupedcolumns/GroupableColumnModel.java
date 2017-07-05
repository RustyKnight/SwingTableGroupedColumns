/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testgroupedcolumns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 *
 * @author swhitehead
 */
public class GroupableColumnModel extends DefaultTableColumnModel {

	private Map<TableColumn, IColumnGroup> mapColumnGroups;
	private Map<IColumnGroup, List<TableColumn>> mapGroupColumns;

	public GroupableColumnModel() {
		mapColumnGroups = new HashMap<>(25);
		mapGroupColumns = new HashMap<>(25);
	}

	public IColumnGroup addGroup(String title) {

		DefaultColumnGroup group = new DefaultColumnGroup(title);
		mapGroupColumns.put(group, new ArrayList<TableColumn>(25));

		return group;

	}

	public IColumnGroup getColumnGroupFor(TableColumn column) {

		return mapColumnGroups.get(column);

	}

	@Override
	public void removeColumn(TableColumn column) {

		IColumnGroup group = mapColumnGroups.get(column);
		if (group != null) {

			mapColumnGroups.remove(column);
			List<TableColumn> columns = mapGroupColumns.get(group);
			if (columns != null) {

				columns.remove(column);

			}

		}

		super.removeColumn(column);

	}

	protected boolean contains(TableColumn column) {

		boolean contains = false;
		for (int index = 0; index < getColumnCount(); index++) {

			if (getColumn(index).equals(column)) {

				contains = true;

			}

		}

		return contains;

	}
	
	public void moveGroup(IColumnGroup group, int newIndex) {

		int startIndex = getGroupIndex(group);
//		int endIndex = startIndex + getColumnCount(group) - 1;
		
		int direction = newIndex < startIndex ? 1 : -1;
		
		List<TableColumn> columns = new ArrayList<TableColumn>(getGroupColumns(group));
		if (newIndex > startIndex) {
			
			Collections.reverse(columns);
			
		}
		
		System.out.println("....");
		
		for (TableColumn col : columns) {
			
			int oldIndex = getColumnIndex(col.getIdentifier());
			System.out.println("Move " + col.getHeaderValue() + " from " + oldIndex + " to " + newIndex);
			super.moveColumn(oldIndex, newIndex);
			newIndex += direction;
			
		}
		
	}

	@Override
	public void moveColumn(int columnIndex, int newIndex) {

		TableColumn column = getColumn(columnIndex);
		IColumnGroup group = getColumnGroupFor(column);
		if (group != null) {

			int startIndex = getGroupIndex(group);
			int endIndex = startIndex + getColumnCount(group) - 1;

			if (newIndex < startIndex) {
				newIndex = startIndex;
			} else if (newIndex > endIndex) {
				newIndex = endIndex;
			}
			
			int pos = newIndex - startIndex;
			
			List<TableColumn> columns = mapGroupColumns.get(group);
			columns.remove(column);
			columns.add(pos, column);

		}
		
		super.moveColumn(columnIndex, newIndex);
		
	}

	public int getGroupIndex(IColumnGroup group) {

		int index = -1;
		List<TableColumn> columns = mapGroupColumns.get(group);
		if (columns != null && columns.size() > 0) {

			index = getColumnIndex(columns.get(0).getIdentifier());

		}

		return index;

	}

	public int getColumnCount(IColumnGroup group) {

		int size = -1;
		List<TableColumn> columns = mapGroupColumns.get(group);
		if (columns != null) {

			size = columns.size();

		}

		return size;

	}

	protected boolean contains(IColumnGroup group) {

		return mapGroupColumns.containsKey(group);

	}

	public boolean containsColumnn(IColumnGroup group, TableColumn column) {

		IColumnGroup check = mapColumnGroups.get(column);
		return check != null && check.equals(group);

	}

	protected void addToGroup(IColumnGroup group, TableColumn column) {

		if (contains(column) && contains(group)) {

			if (!containsColumnn(group, column)) {

				int groupIndex = getGroupIndex(group);
				int groupCount = getColumnCount(group);

				if (groupIndex > -1) {
					int lastIndex = groupCount + groupIndex;
					// Move the column into the group sequence...
					int columnIndex = getColumnIndex(column.getIdentifier());
					if (columnIndex < groupIndex || columnIndex > lastIndex) {

						moveColumn(columnIndex, lastIndex);

					}
				}

				mapColumnGroups.put(column, group);
				mapGroupColumns.get(group).add(column);

			}

		}

	}

	protected void removeFromGroup(IColumnGroup group, TableColumn column) {

		if (contains(column) && contains(group)) {

			if (containsColumnn(group, column)) {

				int groupIndex = getGroupIndex(group);
				int groupCount = getColumnCount(group);

				mapColumnGroups.remove(column);
				mapGroupColumns.get(group).remove(column);

				if (groupIndex > -1) {
					int lastIndex = (groupCount + groupIndex) - 1;
					// We need to move to just outside the groups influence...
					moveColumn(getColumnIndex(column.getIdentifier()), lastIndex);
				}

			}

		}

	}

	public List<IColumnGroup> getGroups() {

		return Collections.unmodifiableList(new ArrayList<>(mapGroupColumns.keySet()));

	}

	public List<TableColumn> getGroupColumns(IColumnGroup group) {

		List<TableColumn> columns = mapGroupColumns.get(group);
		return columns == null ? Collections.unmodifiableList(new ArrayList<TableColumn>(0)) : Collections.unmodifiableList(columns);

	}

	public interface IColumnGroup {

		public String getTitle();

		public TableCellRenderer getRenderer();

		public void addColumn(TableColumn column);

		public void removeColumn(TableColumn column);

		public TableColumn getColumnAt(int column);

		public int getColumnCount();

		public boolean contains(TableColumn column);
	}

	public class DefaultColumnGroup implements IColumnGroup {

		private TableCellRenderer renderer;
		private String title;

		public DefaultColumnGroup(String title) {
			this.title = title;
		}

		public void setRenderer(TableCellRenderer renderer) {
			this.renderer = renderer;
		}

		@Override
		public TableCellRenderer getRenderer() {
			return renderer;
		}

		@Override
		public String getTitle() {
			return title;
		}

		@Override
		public void addColumn(TableColumn column) {

			addToGroup(this, column);

		}

		@Override
		public void removeColumn(TableColumn column) {

			if (contains(column)) {

				removeFromGroup(this, column);

			}

		}

		@Override
		public TableColumn getColumnAt(int index) {

			List<TableColumn> groupColumns = getGroupColumns(this);
			return index >= 0 && index < groupColumns.size() ? groupColumns.get(index) : null;

		}

		@Override
		public int getColumnCount() {

			return getGroupColumns(this).size();

		}

		@Override
		public boolean contains(TableColumn column) {

			return containsColumnn(this, column);

		}
	}
}
