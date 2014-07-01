/**
 * Base package
 */
package kr.jihee.irnlp_toolkit;

/**
 * Version number of IRNLP_Toolkit
 */
public enum Version {

	UNKNOWN("unknown"), V0_7("0.7.3"), V0_8("0.8.1"), PREVIOUS(V0_7.ver), CURRENT(V0_8.ver);

	private String ver;
	private String project_name = "IRNLP_Toolkit";

	private Version(String ver) {
		this.ver = ver;
	}

	public String toString() {
		return ver;
	}

	public String getProjectVersion() {
		return project_name + " " + ver;
	}
}
