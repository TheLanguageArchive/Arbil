package nl.mpi.arbil.data;

import java.util.Map;

/**
 * Document : FieldUpdateRequest
 * Created on : May 21, 2010, 11:52:47 AM
 * Author : Peter Withers
 */
public class FieldUpdateRequest {

    public String fieldPath;
    public String fieldOldValue;
    public String fieldNewValue;
    public String keyNameValue;
    public String fieldLanguageId;
    public Map<String, Object> attributeValuesMap;

    @Override
    public String toString() {
	return String.format("[%1$s] '%2$s' -> '%3$s'", fieldPath, fieldOldValue, fieldNewValue);
    }
}
