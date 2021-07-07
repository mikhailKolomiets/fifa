package site.fifa.dto.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class MPdata {

    public final String ID_KEY = "id";
    public final String DATA = "data";

    private String id;
    private String data;
    private String elseData;
    private String any;
    private Double price;

    public List<MPdata> getSelfList(Map<String, List<String>> request) {
        List<MPdata> result = Collections.emptyList();
        int listSize = request.get(ID_KEY).size();
        for (int i = 0; i < listSize; i++) {
//77.244.39.136
        }
        return result;
    }

}
