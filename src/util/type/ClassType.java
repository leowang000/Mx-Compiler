package util.type;

import java.util.HashMap;

public class ClassType {
    public HashMap<String, FuncType> funcMap_;
    public HashMap<String, Type> varMap_;

    public ClassType() {
        funcMap_ = new HashMap<>();
        varMap_ = new HashMap<>();
    }

    public ClassType(HashMap<String, FuncType> funcMap, HashMap<String, Type> varMap) {
        funcMap_ = funcMap;
        varMap_ = varMap;
    }
}
