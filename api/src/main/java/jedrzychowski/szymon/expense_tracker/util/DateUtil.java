package jedrzychowski.szymon.expense_tracker.util;

import jedrzychowski.szymon.expense_tracker.config.exception.ParamValidationException;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.util.List;

public class DateUtil {

    public static Pair<LocalDate, LocalDate> validateDateParams(LocalDate startDate,
                                                                LocalDate endDate) {
        //Update startDate and endDate in case of null values
        startDate = startDate == null ? LocalDate.of(0, 1, 1) : startDate;
        endDate = endDate == null ? LocalDate.of(9999, 12, 31) : endDate;

        if (startDate.isAfter(endDate)) {
            throw new ParamValidationException(
                    String.format("startDate (%s) cannot be after endDate (%s).", startDate, endDate)
            );
        }
        return Pair.of(startDate, endDate);
    }
}
