package IR.type;

import java.util.ArrayList;
import java.util.HashMap;

import IR.value.IRValue;

public class IRStructType extends IRType {
    public String name_;
    public ArrayList<IRType> fields_;
    public ArrayList<Integer> offset_;
    public HashMap<String, Integer> varToIdMap_;

    public IRStructType(String name) {
        name_ = name;
        fields_ = new ArrayList<>();
        offset_ = new ArrayList<>();
        varToIdMap_ = new HashMap<>();
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
            return 1;
        }
        return (offset_.get(offset_.size() - 1) + fields_.get(fields_.size() - 1).getSize() + 3) / 4 * 4;
    }

    private static ArrayList<Integer> getOffset(ArrayList<IRType> fields) {
        ArrayList<Integer> res = new ArrayList<>();
        int offset = 0;
        for (var type : fields) {
            int sz = type.getSize();
            offset = (offset + sz - 1) / sz * sz;
            res.add(offset);
            offset += sz;
        }
        return res;
    }
}
