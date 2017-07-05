/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testgroupedcolumns;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.RowSorter;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.basic.BasicTableHeaderUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import sun.swing.SwingUtilities2;
import testgroupedcolumns.GroupableColumnModel.IColumnGroup;

/**
 *
 * @author shane
 */
public class GroupableTableHeaderUI extends BasicTableHeaderUI {

	protected static Cursor resizeCursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
	private int selectedColumnIndex = -1;
	private int rolloverColumn = -1;

	protected TableCellRenderer getRenderer(TableColumn column) {

		TableCellRenderer renderer = column.getHeaderRenderer();
		if (renderer == null) {

			renderer = header.getDefaultRenderer();

		}
		return renderer;

	}

	protected TableCellRenderer getRenderer(GroupableColumnModel.IColumnGroup group) {

		TableCellRenderer renderer = group.getRenderer();
		if (renderer == null) {

			renderer = header.getDefaultRenderer();

		}
		return renderer;

	}

	@Override
	public void paint(Graphics g, JComponent c) {

		Rectangle clipBounds = g.getClipBounds();
		TableColumnModel columnModel = header.getColumnModel();
		if (columnModel == null) {
			return;
		}

		GroupableColumnModel groupModel = null;
		if (columnModel instanceof GroupableColumnModel) {

			groupModel = (GroupableColumnModel) columnModel;

		}

		int column = 0;
		Dimension size = header.getSize();
		Rectangle cellRect = new Rectangle(0, 0, size.width, size.height);
//		int columnMargin = header.getColumnModel().getColumnMargin();
		Rectangle draggedCellRect = null;

		TableColumn draggedColumn = header.getDraggedColumn();
		int draggedIndex = -1;

		IColumnGroup draggedGroup = null;
		if (header instanceof GroupableTableHeader) {

			draggedGroup = ((GroupableTableHeader) header).getDraggedGroup();

		}

		GroupableColumnModel.IColumnGroup currentGroup = null;
		Enumeration<TableColumn> enumeration = header.getColumnModel().getColumns();
		int groupHeight = 0;
		while (enumeration.hasMoreElements()) {
			TableColumn aColumn = enumeration.nextElement();
			if (groupModel != null) {

				GroupableColumnModel.IColumnGroup group = groupModel.getColumnGroupFor(aColumn);
				if (group != currentGroup && group != null) {

					cellRect.y = 0;
					currentGroup = group;
					Dimension groupSize = getGroupSize(currentGroup);
					Rectangle groupBounds = new Rectangle(cellRect);
					groupBounds.setSize(groupSize);

					if (group != draggedGroup) {

						paintCell(g, groupBounds, group);
						cellRect.y += groupSize.height;
						groupHeight = groupSize.height;

					} else {

						Rectangle bounds = getGroupHeaderBoundsFor(aColumn);
						g.setColor(header.getParent().getBackground());
						g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

					}

				} else if (group == null) {

					groupHeight = 0;
					cellRect.y = 0;
					cellRect.height = size.height;

				}

			} else {

				groupHeight = 0;
				cellRect.y = 0;
				cellRect.height = size.height;

			}

			cellRect.width = aColumn.getWidth();
			cellRect.height = size.height - groupHeight;

			if (draggedColumn == aColumn) {

				draggedIndex = column;
				g.setColor(header.getParent().getBackground());
				g.fillRect(cellRect.x, cellRect.y, cellRect.width, cellRect.height);

				draggedCellRect = new Rectangle(cellRect);
				draggedCellRect.x += header.getDraggedDistance();

			} else {

				if (draggedGroup == null || !draggedGroup.contains(aColumn)) {

					paintCell(g, cellRect, column);

				}

			}

			cellRect.x += cellRect.width;
			column++;
		}

		// Paint the dragged column last, so it's on top...
		if (draggedCellRect != null) {

			paintCell(g, draggedCellRect, draggedIndex);

		}

		if (draggedGroup != null) {

			Rectangle bounds = getGroupHeaderBoundsFor(draggedGroup);
			bounds.x += header.getDraggedDistance();
			System.out.println(header.getDraggedDistance());
			paintCell(g, bounds, draggedGroup);

			Dimension groupSize = getGroupSize(draggedGroup);
			bounds.y += groupSize.height;
			int startIndex = groupModel.getGroupIndex(draggedGroup);
			for (int index = 0; index < draggedGroup.getColumnCount(); index++) {

				TableColumn tc = draggedGroup.getColumnAt(index);
				bounds.width = tc.getWidth();
				paintCell(g, bounds, startIndex + index);

				bounds.x += bounds.width;

			}

		}

		rendererPane.removeAll();

	}

	public Rectangle getColumnBounds(TableColumn column) {

		Rectangle bounds = new Rectangle();
		bounds.height = header.getHeight();

		TableColumnModel columnModel = header.getColumnModel();
		int colIndex = columnModel.getColumnIndex(column.getIdentifier());
		if (colIndex > -1) {

			for (int index = 0; index < colIndex; index++) {

				bounds.x += columnModel.getColumn(colIndex).getWidth();

			}

		}

		Rectangle groupBounds = getGroupHeaderBoundsFor(column);
		bounds.y = groupBounds.y + groupBounds.height;
		bounds.height -= bounds.y;

		return bounds;

	}

	protected void paintCell(Graphics g, Component component, Rectangle cellRect) {

		rendererPane.add(component);
		rendererPane.paintComponent(
						g,
						component,
						header,
						cellRect.x,
						cellRect.y,
						cellRect.width,
						cellRect.height,
						true);

	}

	protected Component getHeaderRenderer(int columnIndex) {

		TableColumn aColumn = header.getColumnModel().getColumn(columnIndex);
		TableCellRenderer renderer = getRenderer(aColumn);

		boolean hasFocus = !header.isPaintingForPrint()
						&& (columnIndex == getSelectedColumnIndex());
//				&& header.hasFocus();

		return renderer.getTableCellRendererComponent(
						header.getTable(),
						aColumn.getHeaderValue(),
						false, hasFocus,
						-1, columnIndex);

	}

	protected void paintCell(Graphics g, Rectangle cellRect, int columnIndex) {

//		TableColumn aColumn = header.getColumnModel().getColumn(columnIndex);
//		TableCellRenderer renderer = getRenderer(aColumn);

		paintCell(g, getHeaderRenderer(columnIndex), cellRect);

	}

	protected void paintCell(Graphics g, Rectangle cellRect, GroupableColumnModel.IColumnGroup group) {

		boolean hasFocus = false;
		if (getSelectedColumnIndex() > -1) {

			TableColumn selectedColumn = header.getColumnModel().getColumn(getSelectedColumnIndex());
			if (group.contains(selectedColumn)) {

				hasFocus = true;

			}

		}

		TableCellRenderer renderer = getRenderer(group);
		Component component = renderer.getTableCellRendererComponent(
						header.getTable(),
						group.getTitle(),
						false,
						hasFocus,
						-1,
						-1);

		paintCell(g, component, cellRect);

	}

	protected Dimension getGroupSize(GroupableColumnModel.IColumnGroup group) {

		Dimension size = new Dimension();

		TableColumnModel columnModel = header.getColumnModel();
		if (columnModel instanceof GroupableColumnModel) {

			GroupableColumnModel groupModel = (GroupableColumnModel) columnModel;

			TableCellRenderer renderer = getRenderer(group);
			Component comp = renderer.getTableCellRendererComponent(header.getTable(), group.getTitle(), false, false, -1, -1);
			size.height += comp.getPreferredSize().height;

			for (TableColumn column : groupModel.getGroupColumns(group)) {

				size.width += column.getWidth();

			}

		}

		return size;

	}

	protected int calculateHeaderHeight() {

		int height = 0;
		TableColumnModel columnModel = header.getColumnModel();
		for (int column = 0; column < columnModel.getColumnCount(); column++) {

			TableColumn aColumn = columnModel.getColumn(column);
			TableCellRenderer renderer = getRenderer(aColumn);
			Component comp = renderer.getTableCellRendererComponent(header.getTable(), aColumn.getHeaderValue(), false, false, -1, column);
			int cHeight = comp.getPreferredSize().height;

			if (columnModel instanceof GroupableColumnModel) {

				GroupableColumnModel groupModel = (GroupableColumnModel) columnModel;
				List<GroupableColumnModel.IColumnGroup> groups = groupModel.getGroups();
				int maxHeight = 0;
				for (GroupableColumnModel.IColumnGroup group : groups) {

					renderer = getRenderer(group);
					comp = renderer.getTableCellRendererComponent(header.getTable(), group.getTitle(), false, false, -1, column);
					maxHeight = Math.max(maxHeight, comp.getPreferredSize().height);

				}
				cHeight += maxHeight;

			}

			height = Math.max(height, cHeight);

		}

		return height;

	}

	@Override
	public Dimension getPreferredSize(JComponent c) {
		int width = 0;
		Enumeration enumeration = header.getColumnModel().getColumns();
		while (enumeration.hasMoreElements()) {
			TableColumn aColumn = (TableColumn) enumeration.nextElement();
			width = width + aColumn.getPreferredWidth();
		}
		return new Dimension(width, calculateHeaderHeight());
	}

	protected int getSelectedColumnIndex() {
		int numCols = header.getColumnModel().getColumnCount();
		if (selectedColumnIndex >= numCols && numCols > 0) {
			selectedColumnIndex = numCols - 1;
		}
		return selectedColumnIndex;
	}

	protected void repaintColumn(int columnIndex) {

		if (columnIndex >= 0 && columnIndex < header.getColumnModel().getColumnCount()) {

			Rectangle repaintRect = header.getHeaderRect(columnIndex);
			header.repaint(repaintRect);
			GroupableColumnModel groupModel = getGroupableColumnModel();
			if (groupModel != null) {

				TableColumn column = groupModel.getColumn(columnIndex);
				groupModel.getColumnGroupFor(column);
				Rectangle bounds = getGroupHeaderBoundsFor(column);
				header.repaint(bounds);

			}

		}

	}

	protected void selectColumn(int newColIndex, boolean doScroll) {

		int oldIndex = selectedColumnIndex;

		Rectangle repaintRect = header.getHeaderRect(selectedColumnIndex);
		header.repaint(repaintRect);
		selectedColumnIndex = newColIndex;
		repaintRect = header.getHeaderRect(newColIndex);
		header.repaint(repaintRect);
		if (doScroll) {

			scrollToColumn(newColIndex);

		}

		GroupableColumnModel groupModel = getGroupableColumnModel();
		if (groupModel != null) {

			if (oldIndex > -1) {

				TableColumn column = groupModel.getColumn(oldIndex);
				groupModel.getColumnGroupFor(column);
				Rectangle bounds = getGroupHeaderBoundsFor(column);
				header.repaint(bounds);

			}

			if (selectedColumnIndex > -1) {

				TableColumn column = groupModel.getColumn(selectedColumnIndex);
				groupModel.getColumnGroupFor(column);
				Rectangle bounds = getGroupHeaderBoundsFor(column);
				header.repaint(bounds);

			}

		}

	}

	protected GroupableColumnModel getGroupableColumnModel() {

		GroupableColumnModel groupModel = null;
		TableColumnModel tableModel = header.getColumnModel();
		if (tableModel instanceof GroupableColumnModel) {

			groupModel = (GroupableColumnModel) tableModel;

		}

		return groupModel;

	}

	/**
	 * Used by selectColumn to scroll horizontally, if necessary, to ensure that
	 * the newly selected column is visible.
	 */
	protected void scrollToColumn(int col) {
		Container container;
		JTable table;

		//Test whether the header is in a scroll pane and has a table.
		if ((header.getParent() == null)
						|| ((container = header.getParent().getParent()) == null)
						|| !(container instanceof JScrollPane)
						|| ((table = header.getTable()) == null)) {
			return;
		}

		//Now scroll, if necessary.
		Rectangle vis = table.getVisibleRect();
		Rectangle cellBounds = table.getCellRect(0, col, true);
		vis.x = cellBounds.x;
		vis.width = cellBounds.width;
		table.scrollRectToVisible(vis);
	}

	protected MouseInputListener createMouseInputListener() {
		return new MouseInputHandler();
	}

	protected boolean canResize(Point p, TableColumn column,
					JTableHeader header) {

		Rectangle bounds = getGroupHeaderBoundsFor(column);

		return (bounds != null && !bounds.contains(p))
						&& (column != null)
						&& header.getResizingAllowed()
						&& column.getResizable();
	}

	protected void updateRolloverColumn(MouseEvent e) {
		if (header.getDraggedColumn() == null
						&& header.contains(e.getPoint())) {

			int col = header.columnAtPoint(e.getPoint());
			if (col != rolloverColumn) {
				int oldRolloverColumn = rolloverColumn;
				rolloverColumn = col;
				rolloverColumnUpdated(oldRolloverColumn, rolloverColumn);
			}
		}
	}

	protected int changeColumnWidth(TableColumn resizingColumn,
					JTableHeader th,
					int oldWidth, int newWidth) {
		resizingColumn.setWidth(newWidth);

		Container container;
		JTable table;

		if ((th.getParent() == null)
						|| ((container = th.getParent().getParent()) == null)
						|| !(container instanceof JScrollPane)
						|| ((table = th.getTable()) == null)) {
			return 0;
		}

		if (!container.getComponentOrientation().isLeftToRight()
						&& !th.getComponentOrientation().isLeftToRight()) {
			JViewport viewport = ((JScrollPane) container).getViewport();
			int viewportWidth = viewport.getWidth();
			int diff = newWidth - oldWidth;
			int newHeaderWidth = table.getWidth() + diff;

			/* Resize a table */
			Dimension tableSize = table.getSize();
			tableSize.width += diff;
			table.setSize(tableSize);

			/* If this table is in AUTO_RESIZE_OFF mode and
			 * has a horizontal scrollbar, we need to update
			 * a view's position.
			 */
			if ((newHeaderWidth >= viewportWidth)
							&& (table.getAutoResizeMode() == JTable.AUTO_RESIZE_OFF)) {
				Point p = viewport.getViewPosition();
				p.x = Math.max(0, Math.min(newHeaderWidth - viewportWidth,
								p.x + diff));
				viewport.setViewPosition(p);
				return diff;
			}
		}
		return 0;
	}

	protected int viewIndexForColumn(TableColumn aColumn) {
		TableColumnModel cm = header.getColumnModel();
		for (int column = 0; column < cm.getColumnCount(); column++) {
			if (cm.getColumn(column) == aColumn) {
				return column;
			}
		}
		return -1;
	}

	public Rectangle getGroupHeaderBoundsFor(TableColumn column) {

		Rectangle bounds = new Rectangle();

		TableColumnModel columnModel = header.getColumnModel();
		if (columnModel instanceof GroupableColumnModel) {

			GroupableColumnModel groupModel = (GroupableColumnModel) columnModel;
			GroupableColumnModel.IColumnGroup group = groupModel.getColumnGroupFor(column);
			bounds = getGroupHeaderBoundsFor(group);

		}

		return bounds;

	}

	public Rectangle getGroupHeaderBoundsFor(IColumnGroup group) {

		Rectangle bounds = new Rectangle();

		TableColumnModel columnModel = header.getColumnModel();
		if (columnModel instanceof GroupableColumnModel) {

			GroupableColumnModel groupModel = (GroupableColumnModel) columnModel;
			if (group != null) {

				TableColumn firstColumn = group.getColumnAt(0);

				Dimension size = getGroupSize(group);
				bounds = new Rectangle(size);
				bounds.y = 0;

				int lastColumnIndex = columnModel.getColumnIndex(firstColumn.getIdentifier());

				for (int index = 0; index < lastColumnIndex; index++) {

					TableColumn tc = columnModel.getColumn(index);
					bounds.x += tc.getWidth();

				}

			}

		}

		return bounds;

	}

	public class MouseInputHandler implements MouseInputListener {

		private int mouseXOffset;
		private Cursor otherCursor = resizeCursor;

		@Override
		public void mouseClicked(MouseEvent e) {

			if (!header.isEnabled()) {
				return;
			}

			boolean groupHeaderClicked = false;

			TableColumnModel columnModel = header.getColumnModel();
			if (columnModel instanceof GroupableColumnModel) {

				GroupableColumnModel groupModel = (GroupableColumnModel) columnModel;
				int index = header.columnAtPoint(e.getPoint());
				TableColumn column = groupModel.getColumn(index);
				Rectangle bounds = getGroupHeaderBoundsFor(column);
				groupHeaderClicked = bounds.contains(e.getPoint());

			}

			if (!groupHeaderClicked) {

				if (e.getClickCount() % 2 == 1
								&& SwingUtilities.isLeftMouseButton(e)) {

					JTable table = header.getTable();
					RowSorter sorter;
					if (table != null && (sorter = table.getRowSorter()) != null) {

						int columnIndex = header.columnAtPoint(e.getPoint());
						if (columnIndex != -1) {

							columnIndex = table.convertColumnIndexToModel(columnIndex);
							// Possibility to add additional sorting here //
							sorter.toggleSortOrder(columnIndex);

//							Rectangle repaintRect = header.getHeaderRect(columnIndex);
							header.repaint();

						}

					}

				}

			}

		}

		private TableColumn getResizingColumn(Point p) {

			return getResizingColumn(p, header.columnAtPoint(p));

		}

		private TableColumn getResizingColumn(Point p, int column) {

			if (column == -1) {

				return null;

			}

			Rectangle r = header.getHeaderRect(column);
			r.grow(-3, 0);
			if (r.contains(p)) {

				return null;

			}

			int midPoint = r.x + r.width / 2;
			int columnIndex;
			if (header.getComponentOrientation().isLeftToRight()) {

				columnIndex = (p.x < midPoint) ? column - 1 : column;

			} else {

				columnIndex = (p.x < midPoint) ? column : column - 1;

			}

			if (columnIndex == -1) {

				return null;

			}

			return header.getColumnModel().getColumn(columnIndex);

		}

		@Override
		public void mousePressed(MouseEvent e) {

			if (!header.isEnabled()) {
				return;
			}
			header.setDraggedColumn(null);
			header.setResizingColumn(null);
			header.setDraggedDistance(0);

			Point p = e.getPoint();

			// First find which header cell was hit
			TableColumnModel columnModel = header.getColumnModel();
			GroupableColumnModel groupModel = null;
			if (columnModel instanceof GroupableColumnModel) {

				groupModel = (GroupableColumnModel) columnModel;

			}
			int index = header.columnAtPoint(p);

			if (index != -1) {

				boolean clickedGroupHeader = false;
				if (groupModel != null) {

					TableColumn column = columnModel.getColumn(index);
					Rectangle groupBounds = getGroupHeaderBoundsFor(column);
					if (groupBounds.contains(p)) {

						clickedGroupHeader = true;
						mouseXOffset = p.x;

					}

				}

				if (!clickedGroupHeader) {

					// The last 3 pixels + 3 pixels of next column are for resizing
					TableColumn resizingColumn = getResizingColumn(p, index);
					if (canResize(e.getPoint(), resizingColumn, header)) {

						header.setResizingColumn(resizingColumn);
						if (header.getComponentOrientation().isLeftToRight()) {

							mouseXOffset = p.x - resizingColumn.getWidth();

						} else {

							mouseXOffset = p.x + resizingColumn.getWidth();

						}

					} else if (header.getReorderingAllowed()) {

						TableColumn hitColumn = columnModel.getColumn(index);
						header.setDraggedColumn(hitColumn);
						mouseXOffset = p.x;

					}

				} else {

					System.out.println("Clicked group header");
					int columnIndex = header.columnAtPoint(p);
					TableColumn column = columnModel.getColumn(columnIndex);
					IColumnGroup columnGroup = groupModel.getColumnGroupFor(column);
					((GroupableTableHeader) header).setDraggedGroup(columnGroup);

				}

				if (header.getReorderingAllowed()) {

					int oldRolloverColumn = rolloverColumn;
					rolloverColumn = -1;
					rolloverColumnUpdated(oldRolloverColumn, rolloverColumn);

				}

			}

		}

		private void swapCursor() {
			Cursor tmp = header.getCursor();
			header.setCursor(otherCursor);
			otherCursor = tmp;
		}

		@Override
		public void mouseMoved(MouseEvent e) {

			if (!header.isEnabled()) {

				return;

			}

			int index = header.columnAtPoint(e.getPoint());
			selectColumn(index, false);

			TableColumn resizingColumn = getResizingColumn(e.getPoint());
			if (canResize(e.getPoint(), resizingColumn, header) != (header.getCursor() == resizeCursor)) {

				swapCursor();

			}

			updateRolloverColumn(e);

		}

		@Override
		public void mouseDragged(MouseEvent e) {

			if (!header.isEnabled()) {

				return;

			}

			int mouseX = e.getX();

			TableColumn resizingColumn = header.getResizingColumn();
			TableColumn draggedColumn = header.getDraggedColumn();

			boolean headerLeftToRight = header.getComponentOrientation().isLeftToRight();

			IColumnGroup draggedGroup = null;
			if (header instanceof GroupableTableHeader) {

				draggedGroup = ((GroupableTableHeader) header).getDraggedGroup();

			}
			GroupableColumnModel groupModel = getGroupableColumnModel();
			GroupableColumnModel.IColumnGroup currentGroup = null;

			if (draggedGroup != null && groupModel != null) {

				int startIndex = groupModel.getGroupIndex(draggedGroup);
				int endIndex = startIndex + draggedGroup.getColumnCount() - 1;
				int draggedDistance = mouseX - mouseXOffset;
				int direction = (draggedDistance < 0) ? -1 : 1;
				int columnIndex = direction < 0 ? startIndex - 1 : endIndex + 1;
				if (columnIndex < 0) {

					columnIndex = 0;

				} else if (columnIndex >= groupModel.getColumnCount()) {

					columnIndex = groupModel.getColumnCount() - 1;

				}
//				int newColumnIndex = columnIndex + (headerLeftToRight ? direction : -direction);
				int newColumnIndex = columnIndex;
				int width = groupModel.getColumn(newColumnIndex).getWidth();

				IColumnGroup newGroup = groupModel.getColumnGroupFor(groupModel.getColumn(newColumnIndex));
				if (newGroup != null) {

					width = getGroupSize(newGroup).width;
					int groupStartIndex = groupModel.getGroupIndex(newGroup);
					int groupEndIndex = groupModel.getColumnCount(newGroup) + groupStartIndex - 1;

					if (direction < 0) {

						newColumnIndex = groupStartIndex;

					} else {

						newColumnIndex = groupEndIndex;

					}

				}

				if (Math.abs(draggedDistance) > (width / 2)) {

					int selectedIndex
									= SwingUtilities2.convertColumnIndexToModel(
													header.getColumnModel(),
													getSelectedColumnIndex());

					if (newColumnIndex >= 0 && newColumnIndex < groupModel.getColumnCount()) {

						mouseXOffset = mouseXOffset + direction * width;
						header.setDraggedDistance(draggedDistance - direction * width);

						groupModel.moveGroup(draggedGroup, newColumnIndex);

					}

					int viewIndex = SwingUtilities2.convertColumnIndexToView(header.getColumnModel(), selectedIndex);
					selectColumn(
									SwingUtilities2.convertColumnIndexToView(
													header.getColumnModel(), selectedIndex),
									false);

				}

				int range = endIndex - startIndex;
				if (newColumnIndex < startIndex) {

					range = endIndex - newColumnIndex;
					startIndex = newColumnIndex;

				}

				for (int index = -1; index < range + 1; index++) {
					repaintColumn(startIndex + index);
				}

				setDraggedDistance(draggedDistance, newColumnIndex);

			} else {

				if (resizingColumn != null) {

					int oldWidth = resizingColumn.getWidth();
					int newWidth;
					if (headerLeftToRight) {

						newWidth = mouseX - mouseXOffset;

					} else {

						newWidth = mouseXOffset - mouseX;

					}

					mouseXOffset += changeColumnWidth(resizingColumn, header, oldWidth, newWidth);

				} else if (draggedColumn != null) {

					TableColumnModel cm = header.getColumnModel();

					int startIndex = -1;
					int endIndex = -1;
					// Determine the current group that the drag column exists to, if any
					if (groupModel != null) {

						currentGroup = groupModel.getColumnGroupFor(draggedColumn);
						if (currentGroup != null) {

							// Determine the min/max index that the column can be moved to
							startIndex = groupModel.getGroupIndex(currentGroup);
							endIndex = startIndex + currentGroup.getColumnCount() - 1;

						}

					}

					int columnIndex = viewIndexForColumn(draggedColumn);
					int draggedDistance = mouseX - mouseXOffset;
					int direction = (draggedDistance < 0) ? -1 : 1;
					int newColumnIndex = columnIndex + (headerLeftToRight ? direction : -direction);

					boolean shouldMove = true;
					if (startIndex > -1) {

						if (startIndex == columnIndex && direction < 0) {

							shouldMove = false;

						} else if (endIndex == columnIndex && direction > 0) {

							shouldMove = false;

						}

					}

					if (shouldMove) {

						if (groupModel != null && currentGroup == null) {

							GroupableColumnModel.IColumnGroup newGroup = null;
							if (newColumnIndex >= 0 && newColumnIndex < cm.getColumnCount()) {

								newGroup = groupModel.getColumnGroupFor(groupModel.getColumn(newColumnIndex));
								GroupableColumnModel.IColumnGroup oldGroup = newGroup;

								boolean nextStep = true;

								while (nextStep) {

									newColumnIndex += (headerLeftToRight ? direction : -direction);
									if (newColumnIndex < 0) {

										newColumnIndex = 0;
										nextStep = false;

									} else if (newColumnIndex >= groupModel.getColumnCount()) {

										newColumnIndex = groupModel.getColumnCount() - 1;
										nextStep = false;

									} else {

										newGroup = groupModel.getColumnGroupFor(groupModel.getColumn(newColumnIndex));
										if (newGroup != oldGroup) {
											nextStep = false;
											newColumnIndex -= (headerLeftToRight ? direction : -direction);
										}

									}

								}

							} else {

								shouldMove = false;

							}

						}

						if (shouldMove) {

							if (0 <= newColumnIndex && newColumnIndex < cm.getColumnCount()) {

								int width = cm.getColumn(newColumnIndex).getWidth();

								if (groupModel != null && currentGroup == null) {

									IColumnGroup newGroup = groupModel.getColumnGroupFor(groupModel.getColumn(newColumnIndex));
									if (newGroup != null) {

										width = getGroupSize(newGroup).width;

									}

								}

								if (Math.abs(draggedDistance) > (width / 2)) {

									shouldMove = true;

									if (groupModel != null) {

										GroupableColumnModel.IColumnGroup group = groupModel.getColumnGroupFor(draggedColumn);
										if (group != null) {

											shouldMove = newColumnIndex >= startIndex && newColumnIndex <= endIndex;

										}

									}

									//Cache the selected column.
									int viewIndex = getSelectedColumnIndex();
									int selectedIndex
													= SwingUtilities2.convertColumnIndexToModel(
																	header.getColumnModel(),
																	viewIndex);

									if (shouldMove) {

										mouseXOffset = mouseXOffset + direction * width;
										header.setDraggedDistance(draggedDistance - direction * width);

										//Now do the move.
										cm.moveColumn(columnIndex, newColumnIndex);

									}

									//Update the selected index.
									viewIndex = SwingUtilities2.convertColumnIndexToView(header.getColumnModel(), selectedIndex);
									selectColumn(
													SwingUtilities2.convertColumnIndexToView(
																	header.getColumnModel(), selectedIndex),
													false);

								}

							}

							setDraggedDistance(draggedDistance, columnIndex);

						}

					} else {

						header.setDraggedDistance(0);

						if (columnIndex > 0) {

							repaintColumn(columnIndex - 1);

						}
						if (columnIndex < header.getColumnModel().getColumnCount() - 1) {

							repaintColumn(columnIndex + 1);

						}

						repaintColumn(columnIndex);

						setDraggedDistance(0, columnIndex);

					}

				}

			}

			updateRolloverColumn(e);

		}

		public void mouseReleased(MouseEvent e) {
			if (!header.isEnabled()) {
				return;
			}

			if (header instanceof GroupableTableHeader) {
				((GroupableTableHeader) header).setDraggedGroup(null);
			}
			setDraggedDistance(0, viewIndexForColumn(header.getDraggedColumn()));

			header.setResizingColumn(null);
			header.setDraggedColumn(null);

			updateRolloverColumn(e);

			header.repaint();

		}

		public void mouseEntered(MouseEvent e) {
			if (!header.isEnabled()) {
				return;
			}
			updateRolloverColumn(e);
		}

		public void mouseExited(MouseEvent e) {
			if (!header.isEnabled()) {
				return;
			}
			int oldRolloverColumn = rolloverColumn;
			rolloverColumn = -1;
			rolloverColumnUpdated(oldRolloverColumn, rolloverColumn);
			selectColumn(-1, false);
		}
//
// Protected & Private Methods
//

		private void setDraggedDistance(int draggedDistance, int column) {
			header.setDraggedDistance(draggedDistance);
			if (column != -1) {
				header.getColumnModel().moveColumn(column, column);
			}
		}
	}
}
