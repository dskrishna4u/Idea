package com.in10s.rasserver;

public class rsserver {
	public enum NServers {
		NSERVERS(5);

		private final int num_of_servers;

		NServers(int num_of_servers) {
			this.num_of_servers = num_of_servers;
		}

		public int getNumOfServers() {
			return num_of_servers;
		}
	}

	public enum serverIDs {
		ATN(4),
		PDS(3),
		PMS(2),
		IDX(1),
		FOS(0);

		private final int svrID;

		serverIDs(int svrID) {
			this.svrID = svrID;
		}

		public int getserverID() {
			return svrID;
		}
	}
}
