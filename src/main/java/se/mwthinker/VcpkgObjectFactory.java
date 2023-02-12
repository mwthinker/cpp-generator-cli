package se.mwthinker;

public class VcpkgObjectFactory {

    public VcpkgObject createVcpkgObject(String name, String description) {
        var vcpkgObject = new VcpkgObject();
        vcpkgObject.setName(name);
        vcpkgObject.setDescription(description);
        return vcpkgObject;
    }
}
