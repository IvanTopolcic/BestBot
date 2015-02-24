@include ('includes.header')

<div class="main-content">
	@if($errors->any())
		<div class="error">{{{ $errors->first() }}}</div>
	@endif
	<div class="container" style="margin-top:20px">
		<div class="col-md-4 col-md-offset-4">
			<div class="panel panel-default">
				<div class="panel-heading"><h3 class="panel-title"><strong>Log in </strong></h3></div>
				<div class="panel-body">
					<form action="/login" method="post" role="form">
						<div class="form-group">
							<label for="username">Username</label>
							<input name="username" type="text" class="form-control" style="border-radius:0px" id="username" placeholder="Enter username">
						</div>
						<div class="form-group">
							<label for="password">Password <a id="forgot-password" href="#">(forgot password?)</a></label>
							<input name="password" type="password" class="form-control" style="border-radius:0px" id="password" placeholder="Password">
						</div>
						<button type="submit" class="btn btn-sm btn-primary">Sign in</button>
					</form>
				</div>
			</div>
		</div>
	</div>
</div>

@include ('includes.footer')