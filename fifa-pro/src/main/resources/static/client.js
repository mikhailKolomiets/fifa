//connecting to our signaling server
var conn = new WebSocket('ws://192.168.0.104:8080/socket');

conn.onopen = function() {

    console.log("Connected to the signaling server");
    initialize();
};

conn.onmessage = function(msg) {
    console.log("Got message", msg.data);
    var content = JSON.parse(msg.data);
    var data = content.data;
    switch (content.event) {
    // when somebody wants to call us
    case "offer":
        //offerData = data;
        //console.log('from handler' + data)
        //addToken(data);
        //$("#stream-info").text('stream ready...')
        handleOffer(data)
        break;
    case "answer":
        handleAnswer(data);
        break;
    // when a remote peer sends an ice candidate to us
    case "candidate":
        handleCandidate(data);
        break;
    default:
        break;
    }
};

function send(message) {
    conn.send(JSON.stringify(message));
}

var offers;
var peerConnection = null;
var dataChannel;
var input = document.getElementById("messageInput");
var player = document.getElementById("player");
var playerMe = document.getElementById("player-me");

function initialize() {
if (peerConnection != null) {
    console.log('description: ' + peerConnection.currentLocalDescription)
} else {
    peerConnection = new RTCPeerConnection()
}

    // Setup ice handling
    peerConnection.onicecandidate = function(event) {
        if (event.candidate) {
            send({
                event : "candidate",
                data : event.candidate
            });
        }
    };

    peerConnection.onconnectionstatechange = function(event) {
        console.log('connection change to ' + peerConnection.connectionState)
    }

    peerConnection.oniceconnectionstatechange = function(evt) {
    console.log("change ice state: " + peerConnection.iceConnectionState + " connection " + peerConnection.connectionState)
      if (peerConnection.iceConnectionState === "failed") {
        if (peerConnection.restartIce) {
        console.log("restart ice")
          peerConnection.restartIce();
        } else {
          peerConnection.createOffer({ iceRestart: true })
          .then(peerConnection.setLocalDescription)
          .then(sendOfferToServer);
        }
      }
    }

    // creating data channel
    dataChannel = peerConnection.createDataChannel("dataChannel", {
        reliable : true
    });

    dataChannel.onerror = function(error) {
        console.log("Error occured on datachannel:", error);
    };

    // when we receive a message from the other peer, printing it on the console
    dataChannel.onmessage = function(event) {
        console.log("message:", event.data);
    };

    dataChannel.onclose = function() {
        console.log("data channel is closed");
        conn = null;
    };

  	peerConnection.ondatachannel = function (event) {
  	    console.log('open chanel')
        dataChannel = event.channel;
  	};

  	peerConnection.ontrack = ev => {
        console.log('try to read a new video stream')
      if (ev.streams && ev.streams[0]) {
        player.srcObject = ev.streams[0];
        //player.play()
        console.log('video cached ' + ev.streams.length + ' track: ' + ev.track)
      }
    }


    console.log('pc: ' + peerConnection + ' ice: ' + peerConnection.iceConnectionState + ' chanel: ' + peerConnection.connectionState)
}
function createOffer() {
    //openCall(peerConnection)
    offerHelper()
}

async function offerHelper() {

    await peerConnection.createOffer().then(function(offer) {
        //openCall(peerConnection)
        return peerConnection.setLocalDescription(offer);
    })
    .catch(function(reason) {
        console.log(reason)
    });
        addToken(peerConnection.localDescription)
        //openCall(peerConnection);
}

async function handleOffer(offer) {
    await peerConnection.setRemoteDescription(JSON.parse(offer))
//    .then(function () {
//      return navigator.mediaDevices.getUserMedia(constraints);
//    })
//    .then(function(stream) {
//      playerMe.srcObject = stream;
//
//      stream.getTracks().forEach(track => peerConnection.addTrack(track, stream));
//    })

    // create and send an answer to an offer
    peerConnection.createAnswer(function(answer) {
        peerConnection.setLocalDescription(answer);
        send({
            event : "answer",
            data : answer
        });
    }, function(error) {
        alert("Error creating an answer");
    });
    //setTimeout(f => initVideoIn(), 3000)
}

function handleCandidate(candidate) {
    peerConnection.addIceCandidate(new RTCIceCandidate(candidate));
    console.log('handle candidat' + candidate)
};

function handleAnswer(answer) {
    peerConnection.setRemoteDescription(new RTCSessionDescription(answer));
    console.log("connection established successfully!!");
};

function sendMessage() {
//player.play()
//openCall(peerConnection)
}

const constraints = {
    video: true,audio : true
//        video : {
//            frameRate : {
//                ideal : 10,
//                max : 15
//            },
//            width : 1280,
//            height : 720,
//            facingMode : "user"
//        }
};
$("#video-connect").click(async f => {

    //initialize()
    await openCall(peerConnection)
    //setTimeout(f => offerHelper(), 1500)
    offerHelper()
    //console.log('video-stream added')
})

let inboundStream = null;
let ls, usertrack;

function openCall(pc) {
    if (peerConnection.iceConnectionState == 'disconnected') {
        console.log('try to restart ice')
        //peerConnection.restartIce()
//        dataChannel = peerConnection.createDataChannel("dataChannel", {
//                reliable : true
//            });
    }
    return navigator.mediaDevices.getUserMedia(constraints)
    .then(function(localStream) {
        ls = localStream;
      document.getElementById("player-me").srcObject = localStream;
      localStream.getTracks().forEach(track => {
      usertrack = peerConnection.addTrack(track, localStream);
      console.log("add stream")
      });
    })
}

$("#off-output").click(f => {
ls.getTracks().forEach( (track) => {
track.stop();
})})

function addToken (token) {
if (token != null) {
token = JSON.stringify(token)
$.ajax({
    url : 'video/add',
    type : 'POST',
    data : {'token' : token},
    success : function(data) {
        console.log("add token ")
    }
})
} else {
    console.log('cant add null token')
}
}


    $.ajax({
        url : 'video/get-all',
        type : 'GET',
        success : function(tokens) {
            offers = tokens;
            var result = '';
            for(i in tokens) {
                result += '<a><input class="hid" type="hidden" value="' + i + '">Token '+ i +'<button class="up">Connect</button> <br>'
            }
            $("#stream-info").html(result);
        }
    })


$(document).on('click', "[class^=up]", async function() {
    await openCall(peerConnection)
    var div = $(this).parents("a");
    var id = div.find('.hid').val();

    handleOffer(offers[id])
});

