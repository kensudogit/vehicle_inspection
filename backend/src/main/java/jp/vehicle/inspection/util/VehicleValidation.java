package jp.vehicle.inspection.util;

import java.util.regex.Pattern;

public final class VehicleValidation {

  private static final Pattern REGISTRATION =
      Pattern.compile("^[\\p{IsHan}\\p{IsHiragana}\\p{IsKatakana}A-Za-z0-9]{1,4}\\d{3}[\\p{IsHiragana}A-Za-z]{1,2}\\d{1,4}$");
  private static final Pattern CHASSIS = Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$");

  private VehicleValidation() {}

  public static boolean isValidRegistrationNumber(String value) {
    if (value == null || value.isBlank()) return false;
    String normalized = value.replaceAll("[\\s　-]", "");
    return REGISTRATION.matcher(normalized).matches();
  }

  public static boolean isValidChassisNumber(String value) {
    if (value == null || value.isBlank()) return false;
    return CHASSIS.matcher(value.trim().toUpperCase()).matches();
  }

  public static String normalizeRegistrationNumber(String value) {
    return value.replaceAll("[\\s　-]", "");
  }

  public static String normalizeChassisNumber(String value) {
    return value.trim().toUpperCase();
  }
}
