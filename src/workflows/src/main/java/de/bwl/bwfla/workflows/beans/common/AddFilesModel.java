/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package de.bwl.bwfla.workflows.beans.common;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

public class AddFilesModel extends LazyDataModel<File> {
	private static final long serialVersionUID = 1140318022485561671L;

	List<File> data;

	public AddFilesModel(List<File> data) {
		this.data = data;
	}

	@Override
	public File getRowData(String rowKey) {	
		return data.get(Integer.parseInt(rowKey));
	}

	@Override
	public Object getRowKey(File f) {
		return data.indexOf(f);
	}

	@Override
	public void setRowIndex(int rowIndex) {
		/*
		 * The following is in ancestor (LazyDataModel): this.rowIndex =
		 * rowIndex == -1 ? rowIndex : (rowIndex % pageSize);
		 */
		if (rowIndex == -1 || getPageSize() == 0) {
			super.setRowIndex(-1);
		} else {
			super.setRowIndex(rowIndex % getPageSize());
		}
	}

	@Override
	public List<File> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
		
		// rowCount
		int dataSize = data.size();
		this.setRowCount(dataSize);

		System.out.println(dataSize);
		System.out.println(first + " " + pageSize);
		
		// paginate
		if (dataSize > pageSize) {
			try {
				return data.subList(first, first + pageSize);
			} catch (IndexOutOfBoundsException e) {
				return data.subList(first, first + (dataSize % pageSize));
			}
		} else {
			return data;
		}
	}
}
