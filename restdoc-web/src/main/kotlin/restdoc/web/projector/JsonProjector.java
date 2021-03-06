package restdoc.web.projector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import restdoc.web.core.Status;
import restdoc.web.util.FieldType;
import restdoc.web.util.Node;
import restdoc.web.util.PathValue;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static restdoc.web.core.StandardKt.throwError;

/**
 * <p>The JsonProjector provided project the flatten json descriptor to json object</p>
 *
 * <p>Project rule</p>
 *
 * <ul>
 *     <li>Each key must be start with English alphabet</li>
 *     <li>Json Array must be end with brackets '[' or ']' ,Index in square brackets is optional. sample: users[] equivalent to users[0]</li>
 *     <li>If json array No index,default index is zero</li>
 * </ul>
 *
 *
 * <p>sample</p>
 *
 * <pre>
 *     {@code
 *
 *          ObjectNode jsonTree = new JsonProjector(Lists.newArrayList(
 *                 new PathValue("users[1][2].name", "value")
 *         )).getJsonTree();
 *     }
 *
 * operation result As follows json code
 *
 * {@code
 * {
 *   "users": [
 *     [
 *       {
 *         "name": "value"
 *       }
 *     ]
 *   ]
 *  }
 * }
 * </pre>
 *
 * @author Maple
 * @since 1.0.RELEASE
 */
public class JsonProjector extends BaseProjector<ObjectNode> {

    private final ObjectMapper mapper = new ObjectMapper();

    // Filter the field
    private final static Pattern FIELD_PATTERN = compile("[a-zA-Z0-9_]+[a-zA-Z0-9]*");

    // Filter Json array index
    private final static Pattern INDEX_PATTERN = compile("(\\[\\d+\\])+");

    // Filter non number Json array index(default index = 0)
    private final static Pattern NON_NUMBER_INDEX_PATTERN = compile("(\\[\\d*\\])+");

    // Filter Json array field
    // private final static Pattern ARRAY_PATTERN = compile("([a-zA-Z0-9_]+[a-zA-Z0-9_\\-]*)(\\[\\d*\\])+");

    // Filter end with ']'
    // private final static Pattern CHILD_ARRAY_PATTERN = compile("^(.*)\\[\\d+\\]$");

    /**
     * Convert given flatten json descriptor to node tree {@link Node}
     *
     * <pre>
     *     users
     *       ^
     *       |
     *     users[1]
     *       ^
     *       |
     *     users[1][1]
     *       ^
     *       |
     *     users[1][1][1]
     *      /   \
     *    name   age
     * </pre>
     *
     * @param pathValues Given flatten path and json descriptor
     */
    public JsonProjector(List<PathValue> pathValues) {

        // Resolve the json
        List<PathValue> pathValueList = BaseProjector.resolve(pathValues);

        // Build for rootNode
        this.buildForTreeNode(pathValueList);
    }

    public ObjectNode project() {
        // After project. mapping a json tree
        return this.buildForJsonNode();
    }

    public Map<String, Object> projectToMap() {
        return mapper.convertValue(this.project(), Map.class);
    }

    /**
     * Build json node
     *
     * @see JsonNode
     */
    @VisibleForTesting
    protected ObjectNode buildForJsonNode() {
        List<Node> children = this.nodeTree.getChildren();
        ObjectNode on = mapper.createObjectNode();

        for (Node child : children) {
            ObjectNode childNode = new ChildJsonBuilder(child).getObjectNode();
            List<Map.Entry<String, JsonNode>> entries = Lists.newArrayList(childNode.fields());
            if (!entries.isEmpty()) {
                String key = entries.get(0).getKey();
                JsonNode value = entries.get(0).getValue();
                on.putPOJO(key, value);
            }
        }
        return on;
    }

    private class ChildJsonBuilder {
        private final ObjectNode objectNode = mapper.createObjectNode();

        private ChildJsonBuilder(Node parentNode) {
            this.build(parentNode, objectNode);
        }

        ObjectNode getObjectNode() {
            return this.objectNode;
        }

        private void build(Node pn, JsonNode dn) {
            List<Node> children = pn.getChildren();
            String[] fields = pn.getPath().split("\\.");
            String lastField = fields[fields.length - 1];

            if (children.isEmpty()) {
                if (dn instanceof ObjectNode) {
                    ((ObjectNode) dn).putPOJO(lastField, pn.getValue());
                } else if (dn instanceof ArrayNode) {
                    Matcher matcher = ARRAY_PATTERN.matcher(lastField);
                    if (matcher.find()) {
                        String field = matcher.group(1);
                        List<Integer> indexes = BaseProjector.splitIndex(lastField.substring(field.length()));
                        int lastIndex = indexes.get(indexes.size() - 1);
                        ((ArrayNode) dn).insertPOJO(lastIndex, pn.getValue());
                    }
                } else {
                    throwError(Status.INTERNAL_SERVER_ERROR);
                }
            } else {
                FieldType pnType = FieldType.OBJECT;

                if (children.stream().allMatch(node -> node.getPath().matches(String.format("^%s\\[\\d*\\]$", BaseProjector.escape(pn.getPath()))))) {
                    pnType = FieldType.ARRAY;
                }

                if (dn instanceof ObjectNode) {
                    if (pnType == FieldType.OBJECT) {
                        ObjectNode on = mapper.createObjectNode();
                        ((ObjectNode) dn).putPOJO(lastField, on);
                        dn = on;
                    } else {
                        ArrayNode an = mapper.createArrayNode();
                        ((ObjectNode) dn).putPOJO(lastField, an);
                        dn = an;
                    }
                } else if (dn instanceof ArrayNode) {
                    Matcher matcher = ARRAY_PATTERN.matcher(lastField);
                    if (matcher.find()) {
                        String field = matcher.group(1);
                        List<Integer> indexes = BaseProjector.splitIndex(lastField.substring(field.length()));
                        int lastIndex = indexes.get(indexes.size() - 1);

                        if (pnType == FieldType.OBJECT) {
                            ObjectNode on = mapper.createObjectNode();
                            ((ArrayNode) dn).insertPOJO(lastIndex, on);
                            dn = on;
                        } else {
                            ArrayNode an = mapper.createArrayNode();
                            ((ArrayNode) dn).insertPOJO(lastIndex, an);
                            dn = an;
                        }
                    } else {
                        throwError(Status.INTERNAL_SERVER_ERROR);
                    }
                }
                for (Node child : children) {
                    build(child, dn);
                }
            }
        }
    }
}
