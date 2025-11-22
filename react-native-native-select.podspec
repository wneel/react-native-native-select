require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
	s.name            = "react-native-native-select"
	s.version         = package["version"]
	s.summary         = "A strictly native, performant Select component for React Native."
	s.description     = package["description"]
	s.homepage        = "https://github.com/wneel/react-native-native-select"
	s.license         = package["license"]
	s.platforms       = { :ios => "11.0" }
	s.author          = package["author"]
	s.source          = { :git => "https://github.com/wneel/react-native-native-select.git", :tag => "#{s.version}" }

	s.source_files    = "ios/**/*.{h,m,mm,swift}"

	install_modules_dependencies(s)
end
