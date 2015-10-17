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

import java.util.List;

public class RepeatPaginator {
	private int records;
	private int recordsTotal;
	private int pageIndex;
	private int pages;
	private List<?> origModel;
	private List<?> model;
	
	public RepeatPaginator(List<?> model, int records_per_page, int default_page) {
		this.origModel = model;
		this.records = records_per_page;
		this.pageIndex = default_page;
		this.recordsTotal = model.size();		

		if (records > 0) {
			pages = records <= 0 ? 1 : recordsTotal / records;

			if (recordsTotal % records > 0) {
				pages++;
			}

			if (pages == 0) {
				pages = 1;
			}
		} else {
			records = 1;
			pages = 1;
		}

		updateModel();
	}
	
	public RepeatPaginator(List<?> model) {
		this(model, 10, 1);
	}

	public void updateModel() {
		int fromIndex = getFirst();
		int toIndex = getFirst() + records;

		if (toIndex > this.recordsTotal) {
			toIndex = this.recordsTotal;
		}

		this.model = origModel.subList(fromIndex, toIndex);
	}

	public void next() {
		if (this.pageIndex < pages) {
			this.pageIndex++;
		}

		updateModel();
	}

	public void prev() {
		if (this.pageIndex > 1) {
			this.pageIndex--;
		}

		updateModel();
	}

	public int getRecords() {
		return records;
	}

	public int getRecordsTotal() {
		return recordsTotal;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public int getPages() {
		return pages;
	}

	public int getFirst() {
		return (pageIndex * records) - records;
	}

	public List<?> getModel() {
		return model;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}
}