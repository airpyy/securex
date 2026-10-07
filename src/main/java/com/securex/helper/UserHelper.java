package com.securex.helper;

import java.util.UUID;

public class UserHelper {
  public static UUID parseUuid(String uuid) {
	 return UUID.fromString(uuid);
  }
}
