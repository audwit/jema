package edu.mu.mscs.ubicomp.ema.util;

import java.sql.Time;
import java.time.*;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateTimeUtils {

  public static Date toDate(final LocalTime localTime) {
    return new Date(Time.valueOf(localTime).getTime());
  }

  public static Date toDate(final LocalDate localDate) {
    ZonedDateTime zonedDateTime = ZonedDateTime.of(localDate, LocalTime.MIN, ZoneId.systemDefault());
    GregorianCalendar calendar = GregorianCalendar.from(zonedDateTime);
    return calendar.getTime();
  }

  public static Date toDate(final LocalDateTime ldt) {
    ZonedDateTime zonedDateTime = ZonedDateTime.of(ldt, ZoneId.systemDefault());
    GregorianCalendar calendar = GregorianCalendar.from(zonedDateTime);
    return calendar.getTime();
  }
}
