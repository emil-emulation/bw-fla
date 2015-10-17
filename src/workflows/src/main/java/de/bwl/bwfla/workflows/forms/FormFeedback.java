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

package de.bwl.bwfla.workflows.forms;

public class FormFeedback {
	private String editorPrepTask = "";
	private String editorIssues = "";
	private int issues_select = -1;
	private String issueName = "";

	public String getEditorPrepTask() {
		return editorPrepTask;
	}

	public void setEditorPrepTask(String editorPrepTask) {
		this.editorPrepTask = editorPrepTask;
	}

	public String getEditorIssues() {
		return editorIssues;
	}

	public void setEditorIssues(String editorIssues) {
		this.editorIssues = editorIssues;
	}

	public int getIssues_select() {
		return issues_select;
	}

	public void setIssues_select(int issues_select) {
		this.issues_select = issues_select;
	}

	public String getIssueName() {
		return issueName;
	}

	public void setIssueName(String issueName) {
		this.issueName = issueName;
	}
}
