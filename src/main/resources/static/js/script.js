console.log("This is script file")

const toggleSidebar=()=>{
	if($(".sidebar").is(":visible")) {
		$(".sidebar").css("display","none");
		$(".content").css("margin-left","0%");
	}
	
	else{
		$(".sidebar").css("display","block");
		$(".content").css("margin-left","20%");
	}
};

const search=()=>{
	let query=$("#search-input").val()

	if(query=='') {
		$(".search-result").hide();
	}

	else{
		// Search
		// console.log(query);

		// Sending request to server
		let url=`http://localhost:8080/search/${query}`;

		fetch(url).then((response) => {
			return response.json();
		}).then((data)=>{
			// Data ...
			// console.log(data);

			let text=`<div class='list-group'>`

			data.forEach((contact) => {
				text+=`<a href='/user/contact/${contact.cId}' class='list-group-item list-group-item-action'>${contact.name}</a>`
			});

			text+=`</div>`;

			$(".search-result").html(text);
			$(".search-result").show();
		});
	}
};


// First request - To server to create order 

const paymentStart=()=>{
	console.log("Payment Started...");
	let amount=$("#payment_field").val();
	console.log(amount);

	if(amount=="" || amount==null) {
		// alert("Amount is required !!");
		swal("Failed!", "Amount is required !!", "error");
		return;
	}

	// This Code will run if amount is not null

	// We will use ajax to send request to server to create order

	$.ajax({
			url:"/user/create_order",
			data:JSON.stringify({amount:amount,info:"order_request"}),
			contentType:"application/json",
			type: "POST",
			dataType:"json",
			success:function(response){
				// Invoked when success
				console.log(response);
				if(response.status=="created") {
					// Open payment form
					let options={
						key:"rzp_test_kMQGApV6nacP6c",
						amount:response.amount,
						currency:"INR",
						name:"Smart Contact Manager",
						description:"Donation",
						image:"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSh_bzEk2gULO_YAfptpl0mJab5vddjGLHy5w&usqp=CAU",
						order_id:response.id,

						handler:function(response) {
							console.log(response.razorpay_payment_id);
							console.log(response.razorpay_order_id);
							console.log(response.razorpay_signature);
							console.log("Payment successful !!");
							// alert("Congrates !! Payment successful !!");

							updatePaymentOnServer(
								response.razorpay_payment_id,
								response.razorpay_order_id,
								"paid"
							);
						},

						prefill: {
							name: "",
							email: "",
							contact: "",
						},

						notes: {
							address: "LearnCodeWith Mayank",
						},

						theme: {
							color: "#3399cc",
						},
					};

					let rzp = new Razorpay(options);

					rzp.on("payment.failed", function (response){
						console.log(response.error.code);
						console.log(response.error.description);
						console.log(response.error.source);
						console.log(response.error.step);
						console.log(response.error.reason);
						console.log(response.error.metadata.order_id);
						console.log(response.error.metadata.payment_id);
						// alert("Oops payment failed !!");
						swal("Failed!", "Oops payment failed !!", "error");
				});

					rzp.open();
				}
			},
			
			error:function(error){
				// Invoked when error
				console.log(error);
				// alert("Something went wrong !!");
				swal("Failed!", "Something went wrong !!", "error");
			}
		}
	)
};

function updatePaymentOnServer(payment_id,order_id,status)
{
	$.ajax({
		url:"/user/update_order",
		data:JSON.stringify({
			payment_id: payment_id,
			order_id: order_id,
			status: status,
		}),
		contentType:"application/json",
		type: "POST",
		dataType:"json",
		success:function(response) {
			swal("Good Job!", "Congrates, Payment successful !!", "success");
		},

		error:function(error){
			swal("Failed!","Your payment is successful, but we didn't get on server, We will contact you as soon as possible", "error"); 
		},
	});
}