package com.github.tools.editor;

import java.util.HashMap;
import java.util.Map;

import com.github.mat.editor.MatColorProperty;
import com.github.mat.editor.MatConstraints;
import com.github.mat.editor.MatParamProperty;
import com.github.mat.editor.MatVec2Property;
import com.github.mat.editor.MatVec3Property;
import com.github.mat.editor.MaterialSerializer;
import com.github.mat.editor.SpinnerFloatModel;
import com.github.mat.editor.SpinnerIntegerModel;
import com.github.mat.editor.SpinnerModel;
import com.jme3.material.MatParam;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.texture.Texture2D;
import com.simsilica.lemur.Axis;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.FillMode;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.RollupPanel;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.props.PropertyPanel;
import com.simsilica.lemur.style.ElementId;

/**
 * 
 * @author capdevon
 */
public class MaterialEditorBuilder extends AbstractEditor<Material> {
    
    private static final SpinnerFloatModel DefaultSpinnerFloatModel = new SpinnerFloatModel(-200f, 200f, 0.1f);
    private static final SpinnerIntegerModel DefaultSpinnerIntModel = new SpinnerIntegerModel(-200, 200, 1);
    
    private final String[] ignoredProperties;
    private final Map<String, SpinnerModel> constraints = new HashMap<>();
    
    /**
     * @param ignoredProperties
     */
    public MaterialEditorBuilder(String... ignoredProperties) {
        this.ignoredProperties = ignoredProperties;
        addConstraints(MatConstraints.getPBRConstraints());
    }
    
    /**
     * Add a new constraint definition.
     * @param paramName
     * @param model
     */
    public final void addConstraint(String paramName, SpinnerModel<?> model) {
        constraints.put(paramName, model);
    }

    /**
     * Copies all of the constraints from the specified map to this map.
     * @param map
     */
    public final void addConstraints(Map<String, SpinnerModel> map) {
        constraints.putAll(map);
    }

    @SuppressWarnings("unchecked")
    public Container buildPanel(Material material) {
        
        System.out.println(MaterialSerializer.serializeToString(material));
        
        String matDef = material.getMaterialDef().getName();
        String matName = material.getName();
        System.out.println("MatDef: " + matDef);
        System.out.println("MatName: " + matName);

        Container container = new Container(new SpringGridLayout(Axis.Y, Axis.X, FillMode.None, FillMode.Even));
        container.addChild(createLabelValue("Material Def:", matDef));
        container.addChild(createLabelValue("Material Name:", matName));

        PropertyPanel propertyPanel = container.addChild(new PropertyPanel("glass"));

        // TODO: sort material params by name
        int numParams = material.getParamsMap().size();
        for (int i = 0; i < numParams; i++) {

            MatParam param = material.getParamsMap().getValue(i);
            System.out.println("MatParam: " + param);
            
            if (ignoreProperty(param)) {
                continue;
            }

            String name = param.getName();
            Object value = param.getValue();

            if (value instanceof ColorRGBA) {
                MatColorProperty mp = new MatColorProperty(name, material);
                container.addChild(mp.buildPanel());

            } else if (value instanceof Vector2f) {
                MatVec2Property mp = new MatVec2Property(name, material);
                container.addChild(mp.buildPanel());

            } else if (value instanceof Vector3f) {
                MatVec3Property mp = new MatVec3Property(name, material);
                container.addChild(mp.buildPanel());

            } else if (value instanceof Float) {
                MatParamProperty<Float> mp = new MatParamProperty<>(name, material);
                SpinnerModel<Float> range = constraints.getOrDefault(name, DefaultSpinnerFloatModel);
                propertyPanel.addFloatProperty(name, mp, "value", range.getMinValue(), range.getMaxValue(), range.getStep());

            } else if (value instanceof Integer) {
                MatParamProperty<Integer> mp = new MatParamProperty<>(name, material);
                SpinnerModel<Integer> range = constraints.getOrDefault(name, DefaultSpinnerIntModel);
                propertyPanel.addIntProperty(name, mp, "value", range.getMinValue(), range.getMaxValue(), range.getStep());

            } else if (value instanceof Boolean) {
                MatParamProperty<Boolean> mp = new MatParamProperty<>(name, material);
                propertyPanel.addBooleanProperty(name, mp, "value");

            } else if (value instanceof Texture2D) {
                // container.addChild(createLabelValue(name + ":", ((Texture2D) value).getName()));
                // MatLabelProperty ???
            }
        }
        
        RenderState renderState = material.getAdditionalRenderState();
        container.addChild(createRenderStateSection(renderState));

        return container;
    }

    private Panel createRenderStateSection(RenderState renderState) {

        PropertyPanel propertyPanel = new PropertyPanel("glass");
        propertyPanel.addEnumProperty("FaceCullMode", renderState, "faceCullMode");
        propertyPanel.addEnumProperty("BlendMode", renderState, "blendMode");
        propertyPanel.addBooleanProperty("Wireframe", renderState, "wireframe");
        propertyPanel.addBooleanProperty("DepthWrite", renderState, "depthWrite");
        propertyPanel.addBooleanProperty("ColorWrite", renderState, "colorWrite");
        propertyPanel.addBooleanProperty("DepthTest", renderState, "depthTest");

        String title = "Additional RenderState";
        RollupPanel rollup = new RollupPanel(title, propertyPanel, "glass");
        rollup.setAlpha(0, false);
        rollup.setOpen(false);

        return rollup;
    }

    private Container createLabelValue(String label, String value) {
        Container container = new Container(new SpringGridLayout(Axis.X, Axis.Y, FillMode.ForcedEven, FillMode.Even));
        container.addChild(new Label(label, new ElementId("label")));
        container.addChild(new Label(value, new ElementId("label-ro")));
        return container;
    }
    
    /**
     * @param param
     * @return
     */
    private boolean ignoreProperty(MatParam param) {
        boolean ignoreProperty = false;
        for (String ignoredProperty : ignoredProperties) {
            if (param.getName().equalsIgnoreCase(ignoredProperty)) {
                ignoreProperty = true;
                break;
            }
        }
        return ignoreProperty;
    }

}