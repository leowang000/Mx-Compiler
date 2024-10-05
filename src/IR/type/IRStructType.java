package IR.type;

import java.util.ArrayList;
import java.util.HashMap;

import IR.value.IRValue;

public class IRStructType extends IRType {
    public String name_;
    public ArrayList<IRType> fields_ = new ArrayList<>();
    public ArrayList<Integer> offset_ = new ArrayList<>();
    public HashMap<String, Integer> varToIdMap_ = new HashMap<>();

    public IRStructType(String name) {
        name_ = name;
    }

    @Override
    public String toString() {
        return "%struct." + name_;
    }

    public void setFields(ArrayList<IRType> fields) {
        fields_ = fields;
        offset_ = getOffset(fields);
    }

    public String getStructInfo() {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < fields_.size(); i++) {
            sb.append(fields_.get(i));
            if (i < fields_.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IRStructType && name_.equals(((IRStructType)obj).name_);
    }

    @Override
    public int getSize() {
        if (fields_.isEmpty()) {
            return 4;
        }
        return 4 * fields_.size();
    }

    private static ArrayList<Integer> getOffset(ArrayList<IRType> fields) {
        ArrayList<Integer> res = new ArrayList<>();
        for (int i = 0; i < fields.size(); i++) {
            res.add(i * 4);
        }
        return res;
    }
}
