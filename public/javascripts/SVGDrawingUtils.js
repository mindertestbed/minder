/**
 * Created by melis on 11/02/15.
 */
var svgns = "http://www.w3.org/2000/svg";
var selectedObjectList=null;
var svgDocument = null;
var activeElementX=0;
var activeElementY=0;
var activeElement = null;
var activeElementOutputLines = null;
var activeElementInputLines = null;
var activeOutputX=0;
var activeOutputY=0;
var activeOutput = null;
var activeInputX=0;
var activeInputY=0;
var activeInput = null;
var activeLine=null;

var activeElementNrOfInput=0;
var activeElementNrOfOutput=0;

var counterSignal=0;
var counterSlot=0;
var counterConverter=0;

var globalSVG = null;
var arr = {};

function initialize(){
    var svg = document.getElementById("SVGCanvas");
    svgDocument = svg.contentDocument;

    arr["slot_1_group_1"]= 4; //slot
    arr["slot_1_group_2"] = 0; // signal
    arr["signal_1_group_1"] = 0;
    arr["signal_1_group_2"] = 5;
    arr["converter_1_group_1"] = 1;
    arr["converter_1_group_2"] = 1;
    arr["slot_2_group_1"]= 4; //slot
    arr["slot_2_group_2"] = 0; // signal
    arr["signal_2_group_1"] = 0;
    arr["signal_2_group_2"] = 5;

    svgDocument.addEventListener("mousemove",function(evt){
        var currentPointerx=evt.clientX;
        var currentPointery=evt.clientY;
        if(null !== activeElement){
            var boundaryRectangle = activeElement.getBoundingClientRect();

            var x1 = evt.clientX - activeElementX;
            var y1 = evt.clientY - activeElementY;

            activeElement.setAttributeNS(null, "transform", "translate(" + x1 + ", " + y1 + ")");
            var activeElementId = activeElement.getAttributeNS(null,"id");

            for(var i=0; i<activeElementNrOfInput; i++) {
                var circleIn = svgDocument.getElementById(activeElement.getAttributeNS(null,"id")+i+"_inputGroup_control_");
                var boundaryRectangleIn = circleIn.getBoundingClientRect();
                var top=boundaryRectangleIn.top;
                var left = boundaryRectangleIn.left;
                var width = boundaryRectangleIn.width;
                var height = boundaryRectangleIn.height;
                if(0 !== activeElementInputLines.length) {
                    for(var j=0; j<activeElementInputLines[i].length; j++) {
                        var endX = left+width/2;
                        var endY = top+height/2;
                        var pathValue = activeElementInputLines[i][j].getAttributeNS(null, "d");

                        var temp = pathValue.split(' ');
                        var start = temp[0];

                        var startTemp = start.split(',');
                        var startY = parseInt(startTemp[1]);
                        var startX = parseInt(startTemp[0].split('M')[1]);

                        var controlX = Math.ceil((startX + endX) / 2 + 20);
                        var controlY = Math.ceil((startY + endY) / 2 + 20);

                        var newPathValue = start+" Q"+controlX+","+controlY+" "+endX+","+endY;
                        activeElementInputLines[i][j].setAttributeNS(null, "d", newPathValue);
                    }
                }
            }

            for(var i=0; i<activeElementNrOfOutput; i++) {
                var circleOut = svgDocument.getElementById(activeElement.getAttributeNS(null,"id")+i+"_outputGroup_control_");
                var boundaryRectangleOut = circleOut.getBoundingClientRect();
                var top=boundaryRectangleOut.top;
                var left = boundaryRectangleOut.left;
                var width = boundaryRectangleOut.width;
                var height = boundaryRectangleOut.height;
                if(0 !== activeElementOutputLines.length) {
                    for(var j=0; j<activeElementOutputLines[i].length; j++) {
                        var startX = left+width/2;
                        var startY = top+height/2;
                        var pathValue = activeElementOutputLines[i][j].getAttributeNS(null, "d");

                        var temp = pathValue.split(' ');
                        var end = temp[2];

                        var endTemp = end.split(',');
                        var endX = parseInt(endTemp[0]);
                        var endY = parseInt(endTemp[1]);

                        var controlX = Math.ceil((startX + endX) / 2 + 20);
                        var controlY = Math.ceil((startY + endY) / 2 + 20);

                        var newPathValue = "M"+startX+","+startY+" Q"+controlX+","+controlY+" "+end;

                        activeElementOutputLines[i][j].setAttributeNS(null, "d", newPathValue);
                    }
                }
            }
        }

        if(null !== activeOutput){
           var pathValue = activeLine.getAttributeNS(null, "d");

            var temp = pathValue.split(' ')
            var start = temp[0];

            var startTemp = start.split(',');
            var startY = parseInt(startTemp[1]);
            var startX = parseInt(startTemp[0].split('M')[1]);

            var controlX = Math.ceil((startX + evt.clientX) / 2 + 20);
            var controlY = Math.ceil((startY + evt.clientY) / 2 + 20);

            var newPathValue = start+" Q"+controlX+","+controlY+" "+evt.clientX+","+evt.clientY;
            activeLine.setAttributeNS(null, "d", newPathValue);

        }
    }, false);

    svgDocument.addEventListener("mouseup",function(evt){
        if(null !== activeOutput){

            var result = isMouseOver(evt.clientX,evt.clientY);
            if(null !== result) {
                activeLine.setAttributeNS(null, "x2", activeInputX);
                activeLine.setAttributeNS(null, "y2", activeInputY);

                activeLine.addEventListener("click",function(evt){
                    selectedObjectList = new Array();
                    var element=evt.target;
                    element.setAttributeNS(null, "stroke", "red");
                    selectedObjectList[0] = element;
                }, false);

                activeLine.addEventListener("focusout",function(evt){
                    var element=evt.target;
                    element.setAttributeNS(null, "stroke", "#828282");
                }, false);

                activeLine.setAttributeNS(null,"id","Output:"+activeOutput.getAttributeNS(null,"id")+"-Input:"+activeInput.getAttributeNS(null,"id")+"-");
                triangle = createDirectionTriangle(activeInputX,activeInputY);
            }else{
                svgDocument.documentElement.removeChild(activeLine);
            }

            activeInput = null;
            activeInputX = 0;
            activeInputY = 0;

            activeOutput=null;
            activeOutputX = 0;
            activeOutputY = 0;

        }
    }, false);

    activeElement = null;
    activeElementInputLines = null;
    activeElementOutputLines = null;
    activeElementNrOfInput = 0;
    activeElementNrOfOutput = 0;
}

function addNewSignal(signalName) {
    if(null === svgDocument){
    	initialize();
    }
    counterSignal++;
    var signalIdBase = "signal_"+counterSignal+"_";
    var groupId = signalIdBase+"group_";

    var signalName = signalName;//"Signal "+ counterSignal;
    var rectHeight = 50;
    var rectWidth = 100;

    var group = createNode(groupId,signalName,rectHeight*5,rectWidth);

    var positionX = rectWidth;
    var positionY = rectHeight;
    var outputName= "Output ";
    for(var i=0; i<5; i++) {
        var tempId=group.getAttributeNS(null,"id")+i+"_outputGroup"+"_";
        var output = addOutputPoint(tempId,positionX,positionY);

        var text = svgDocument.createElementNS(svgns, "text");
        text.setAttributeNS(null, "x",positionX-10);
        text.setAttributeNS(null, "y", positionY);
        text.setAttributeNS(null, "text-anchor", "end");
        text.setAttributeNS(null, "alignment-baseline","middle");

        var textNode = svgDocument.createTextNode(outputName+i);
        text.appendChild(textNode);

        positionY = positionY + rectHeight;
        group.appendChild(output);
        group.appendChild(text);
    }

    svgDocument.documentElement.appendChild(group);

}

function addNewSlot(slotName) {
    if(null === svgDocument){
    	initialize();
    }
    
    counterSlot++;
    var slotIdBase = "slot_"+counterSlot+"_";
    var groupId = slotIdBase+"group_";

    var slotName = slotName;//"Slot "+ counterSlot;
    var rectHeight = 50;
    var rectWidth = 100;

    var group = createNode(groupId,slotName,rectHeight*4,rectWidth);

    var positionX = 0;
    var positionY = rectHeight;
    var inputName= "Input ";
    for(var i=0; i<4; i++) {
        var tempId=group.getAttributeNS(null,"id")+i+"_inputGroup"+"_";
        var input = addInputPoint(tempId,positionX,positionY);

        var text = svgDocument.createElementNS(svgns, "text");
        text.setAttributeNS(null, "x",positionX+10);
        text.setAttributeNS(null, "y", positionY);
        text.setAttributeNS(null, "text-anchor", "start");
        text.setAttributeNS(null, "alignment-baseline","middle");

        var textNode = svgDocument.createTextNode(inputName+i);
        text.appendChild(textNode);

        positionY = positionY + rectHeight;
        group.appendChild(input);
        group.appendChild(text);
    }

    svgDocument.documentElement.appendChild(group);

}

function addNewConverter() {
    if(null === svgDocument){
    	initialize();
    }

    counterConverter++;
    var converterIdBase = "converter_"+counterConverter+"_";
    var groupId = converterIdBase+"group_";

    var converterName = "Converter "+ counterConverter;
    var rectHeight = 50;
    var rectWidth = 200;

    var group = createNode(groupId,converterName,rectHeight,rectWidth);

    var positionX = 0;
    var positionY = rectHeight;
    var inputName= "Input ";
    for(var i=0; i<1; i++) {
        var tempId=group.getAttributeNS(null,"id")+i+"_inputGroup"+"_";
        var input = addInputPoint(tempId,positionX,positionY);

        var text = svgDocument.createElementNS(svgns, "text");
        text.setAttributeNS(null, "x",positionX+10);
        text.setAttributeNS(null, "y", positionY);
        text.setAttributeNS(null, "text-anchor", "start");
        text.setAttributeNS(null, "alignment-baseline","middle");

        var textNode = svgDocument.createTextNode(inputName+i);
        text.appendChild(textNode);

        positionY = positionY + rectHeight;
        group.appendChild(input);
        group.appendChild(text);
    }

    var positionX = rectWidth;
    var positionY = rectHeight;
    var outputName= "Output ";
    for(var i=0; i<1; i++) {
        var tempId=group.getAttributeNS(null,"id")+i+"_outputGroup"+"_";
        var output = addOutputPoint(tempId,positionX,positionY);

        var text = svgDocument.createElementNS(svgns, "text");
        text.setAttributeNS(null, "x",positionX-10);
        text.setAttributeNS(null, "y", positionY);
        text.setAttributeNS(null, "text-anchor", "end");
        text.setAttributeNS(null, "alignment-baseline","middle");

        var textNode = svgDocument.createTextNode(outputName+i);
        text.appendChild(textNode);

        positionY = positionY + rectHeight;
        group.appendChild(output);
        group.appendChild(text);
    }

    svgDocument.documentElement.appendChild(group);

}

function createNode(groupId, nodeName,rectHeight,rectWidth){
    var group = svgDocument.createElementNS(svgns,  "g");

    group.setAttribute("id",groupId );
    group.setAttribute("class","draggable");
    group.setAttribute("transform","translate(0 0)");
    group.$x = 0;
    group.$y = 0;

    var rectangle= createRectangle(groupId+"rectangle_",0,25,20,20,rectWidth,rectHeight,"#FF9933","0.8");

    var text = svgDocument.createElementNS(svgns, "text");
    text.setAttributeNS(null, "id", groupId+"nodename_");
    text.setAttributeNS(null, "x", rectangle.getAttributeNS(null,"width")/2);
    text.setAttributeNS(null, "y", 20);
    text.setAttributeNS(null, "text-anchor", "middle");

    var textNode = svgDocument.createTextNode(nodeName);
    text.appendChild(textNode);

    group.appendChild(rectangle);
    group.appendChild(text);

    return group;
}

function createDirectionTriangle(x,y){
    var triangle = svgDocument.createElementNS(svgns, "path");
    triangle.setAttributeNS(null, "d", "M "+x+" "+(y-5)+" L "+(x+5)+" "+y+" L "+(x)+" "+(y+5)+" z");
    triangle.setAttributeNS(null, "stroke", "red");
    triangle.setAttributeNS(null, "stroke-width", "4");
    triangle.setAttributeNS(null, "stroke-linecap", "round");
    triangle.setAttributeNS(null, "fill", "red");

    return triangle;
}

function createRectangle(id,x,y,rx,ry,width,height,fill,opacity){
    var rect = svgDocument.createElementNS(svgns, "rect");
    rect.setAttributeNS(null, "id", id);
    rect.setAttributeNS(null, "x", x);
    rect.setAttributeNS(null, "y", y);
    rect.setAttributeNS(null, "rx", rx);
    rect.setAttributeNS(null, "ry", ry);
    rect.setAttributeNS(null, "width", width);
    rect.setAttributeNS(null, "height", height);
    rect.setAttributeNS(null, "fill", fill);
    rect.setAttributeNS(null, "opacity", opacity);

    rect.addEventListener("mousedown",function(evt){
        activeElement =  rect.parentNode;
        activeElementInputLines = null;
        activeElementOutputLines = null;
        var transform = activeElement.getAttributeNS(null, "transform");
        //transform is a string like "transform(x y)" thus we split it into 2
        //and get strings transform(x  and y) strings.
        var asdf = transform.split(' ')
        //we split tranform(x with '(' and get the second token as x
        var localX = parseInt(asdf[0].split('(')[1])
        //we split y) with ')' and get the first token as y
        var localY = parseInt(asdf[1].split(')')[0])

        activeElementX = evt.clientX - localX;
        activeElementY = evt.clientY - localY;
        var activeElementId = activeElement.getAttributeNS(null,"id")

        if(0 < arr[activeElement.getAttributeNS(null,"id")+"1"]){
            activeElementInputLines = new Array();
        }
        for(var i=0; i<arr[activeElement.getAttributeNS(null,"id")+"1"]; i++) {
            var foundLines = getLinesOfActiveElement("Input:"+activeElementId+i+"_inputGroup_control_");
            activeElementInputLines[i]=new Array(foundLines.length);
            for(var j=0; j<foundLines.length; j++) {

                activeElementInputLines[i][j] = foundLines[j];
            }
        }
        activeElementNrOfInput= arr[activeElementId+"1"];

        if(0 < arr[activeElement.getAttributeNS(null,"id")+"2"]){
            activeElementOutputLines = new Array();
        }
        for(var i=0; i<arr[activeElement.getAttributeNS(null,"id")+"2"]; i++) {
            var foundLines = getLinesOfActiveElement("Output:"+activeElementId+i+"_outputGroup_control_");
            activeElementOutputLines[i]=new Array(foundLines.length);
            for(var j=0; j<foundLines.length; j++) {
                activeElementOutputLines[i][j] = foundLines[j];
            }
        }
        activeElementNrOfOutput=arr[activeElementId+"2"];
    }, false);


    rect.addEventListener("mouseup",function(evt){
        activeElement=null;
        activeElementInputLines = null;
        activeElementOutputLines = null;
        activeElementX = 0;
        activeElementY = 0;
        activeElementNrOfInput = 0;
        activeElementNrOfOutput = 0;
    }, false);

    rect.addEventListener("click",function(evt){
        selectedObjectList = new Array();
        var selectedObjectId = rect.parentNode.getAttributeNS(null,"id");
        var counter = 0;
        selectedObjectList[counter] = getGroupByName(selectedObjectId);

        if(0 < arr[selectedObjectId+"1"]){
            activeElementInputLines = new Array();
        }
        for(var i=0; i<arr[selectedObjectId+"1"]; i++) {
            var foundLines = getLinesOfActiveElement("Input:"+selectedObjectId+i+"_inputGroup_control_");
            activeElementInputLines[i]=new Array(foundLines.length);
            for(var j=0; j<foundLines.length; j++) {
                counter++;
                activeElementInputLines[i][j] = foundLines[j];
                selectedObjectList[counter] = activeElementInputLines[i][j];
            }
        }
        activeElementNrOfInput= arr[selectedObjectId+"1"];

        if(0 < arr[selectedObjectId+"2"]){
            activeElementOutputLines = new Array();
        }
        for(var i=0; i<arr[selectedObjectId+"2"]; i++) {
            var foundLines = getLinesOfActiveElement("Output:"+selectedObjectId+i+"_outputGroup_control_");
            activeElementOutputLines[i]=new Array(foundLines.length);
            for(var j=0; j<foundLines.length; j++) {
                counter++;
                activeElementOutputLines[i][j] = foundLines[j];
                selectedObjectList[counter] = activeElementOutputLines[i][j];
            }
        }
        activeElementNrOfOutput=arr[selectedObjectId+"2"];

        rect.setAttributeNS(null, "stroke", "red");
        rect.setAttributeNS(null, "stroke-width", "4");
        rect.setAttributeNS(null, "stroke-linecap", "round");
    }, false);

    rect.addEventListener("focusout",function(evt){
        rect.removeAttributeNS(null, "stroke");
        rect.removeAttributeNS(null, "stroke-width");
        rect.removeAttributeNS(null, "stroke-linecap");

        activeElement = null;
        activeElementInputLines = null;
        activeElementOutputLines = null;
        activeElementX = 0;
        activeElementY = 0;
        activeElementNrOfOutput = 0;
        activeElementNrOfInput = 0;
    }, false);
    return rect;
}

function addOutputPoint(id,cx,cy){
    var controlGrp = svgDocument.createElementNS(svgns,  "g");
    controlGrp.setAttribute("id", id);
    controlGrp.setAttribute("transform","translate(0 0)");

    //Control Point
    var invisiblePoint = svgDocument.createElementNS(svgns, "circle");
    invisiblePoint.setAttributeNS(null, "cx", cx);
    invisiblePoint.setAttributeNS(null, "cy", cy);
    invisiblePoint.setAttributeNS(null, "r", 15);
    invisiblePoint.setAttributeNS(null, "class", "control_point");

    var controlPoint = svgDocument.createElementNS(svgns, "circle");
    controlPoint.setAttributeNS(null, "id", id+"control_");
    controlPoint.setAttributeNS(null, "cx", cx);
    controlPoint.setAttributeNS(null, "cy", cy);
    controlPoint.setAttributeNS(null, "r", 10);
    controlPoint.setAttributeNS(null, "class", "pointleft invisible");

    controlGrp.addEventListener("mousedown",function(evt){
        activeOutput =  controlPoint;

        var boundaryRectangle = activeOutput.getBoundingClientRect();
        var top=boundaryRectangle.top;
        var left = boundaryRectangle.left;
        var width = boundaryRectangle.width;
        var height = boundaryRectangle.height;
        activeOutputX = left+width/2;
        activeOutputY = top+height/2;

        var pathValue = "M"+activeOutputX+","+activeOutputY+" Q"+activeOutputX+","+activeOutputY +" "+activeOutputX+","+activeOutputY;
        var path = svgDocument.createElementNS(svgns, "path");
        path.setAttributeNS(null, "d", pathValue);
        path.setAttributeNS(null, "stroke", "#828282");
        path.setAttributeNS(null, "stroke-width", "4");
        path.setAttributeNS(null, "stroke-linecap", "round");
        path.setAttributeNS(null, "fill", "none");
        svgDocument.documentElement.appendChild(path);

        activeLine = path

    }, false);

    controlGrp.appendChild(invisiblePoint);
    controlGrp.appendChild(controlPoint);


    return controlGrp;
}

function addInputPoint(id,cx,cy){
    var controlGrp = svgDocument.createElementNS(svgns,  "g");
    controlGrp.setAttribute("id", id);
    controlGrp.setAttribute("transform","translate(0 0)");

    //Control Point
    var invisiblePoint = svgDocument.createElementNS(svgns, "circle");
    invisiblePoint.setAttributeNS(null, "cx", cx);
    invisiblePoint.setAttributeNS(null, "cy", cy);
    invisiblePoint.setAttributeNS(null, "r", 15);
    invisiblePoint.setAttributeNS(null, "class", "control_point");

    var controlPoint = svgDocument.createElementNS(svgns, "circle");
    controlPoint.setAttributeNS(null, "id", id+"control_");
    controlPoint.setAttributeNS(null, "cx", cx);
    controlPoint.setAttributeNS(null, "cy", cy);
    controlPoint.setAttributeNS(null, "r", 10);
    controlPoint.setAttributeNS(null, "class", "pointright invisible");

    controlGrp.appendChild(invisiblePoint);
    controlGrp.appendChild(controlPoint);

    return controlGrp;
}


function removeSelectedElement() {
    if(null === selectedObjectList) {
        alert("Select an element to delete");
        return;
    }

    for(var i=0; i<selectedObjectList.length; i++) {
        svgDocument.documentElement.removeChild(selectedObjectList[i]);
    }

    selectedObjectList = null;
}

function getGroupByName(id) {
    var groups = svgDocument.getElementsByTagName("g");
    for(var i=0; i<groups.length; i++) {
        if(groups[i].getAttributeNS(null, "id") === id) {
            return groups[i];
        }
    }
    return null;
}

function getLinesOfActiveElement(subId) {
    var foundLines=[];
    var counter = -1;
    var lines = svgDocument.getElementsByTagName("path");
    for(var i=0; i<lines.length; i++) {
        if(lines[i].getAttributeNS(null, "id").indexOf(subId) > -1) {
            counter++;
            foundLines[counter]=lines[i];
        }
    }
    return foundLines;
}

function isMouseOver(x,y) {
    var circles = svgDocument.getElementsByTagName("circle");
    for(var i=0; i<circles.length; i++) {
        if(circles[i].getAttributeNS(null, "id").indexOf("_inputGroup_control_") > -1) {
            var boundaryRectangle = circles[i].getBoundingClientRect();
            var top=boundaryRectangle.top;
            var left = boundaryRectangle.left;
            var width = boundaryRectangle.width;
            var height = boundaryRectangle.height;

            if(left<=x && x<=(left+width) && top<=y && y<=(top+height)){
                activeInput = circles[i];
                activeInputX = left+width/2;
                activeInputY = top+height/2;
                return circles[i];
            }

        }
    }
    return null;
}

function generateXMLofSVG(){
    //"</?xml version="1.0"?>"
    var svgList = svgDocument.getElementsByTagName("svg");
    if (0 === svgList.length){
         alert("There is nothing to save!");
         return;
    }
    var svgTree = svgList[0];
    console.log(svgTree);
    globalSVG = svgTree;
}

function loadSVGFromXML(){
    var svgList = svgDocument.getElementsByTagName("svg");
    if (0 === svgList.length){
        alert("The page does not loaded correctly!");
        return;
    }
    var svgTree = svgList[0];

    var parentNode=svgTree.parentNode;
    parentNode.removeChild(svgTree);
    parentNode.appendChild(globalSVG);
}