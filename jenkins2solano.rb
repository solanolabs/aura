#!/bin/env ruby -w

require 'fileutils'
require 'nokogiri'
require 'optparse'
require 'pathname'
require 'yaml'

SOLANO_DIR_NAME = '.solano'
DEFAULT_CONFIG_NAME = 'solano.yml'
DEFAULT_CONFIG_DATA = {
  java: {java_version: 'java-7-openjdk', maven_version: '3.0'},
  timeout_hook: 1200,
  timeout: 2400,
  tests: [],
  collect: {repo: ['screenshots/**/*']}
}
SUPPORTED_JAVA_VERSIONS = %w[
  java-6-openjdk,
  java-7-openjdk,
  java-7-oracle,
  java-8-oracle,
]
SUPPORTED_MAVEN_VERSIONS = %w[ 2.0, 3.0, 3.1, 3.2 ]


class SolanoConfig

  attr_accessor :data

  def initialize(data=DEFAULT_CONFIG_DATA)
    @data = data
  end

  def java_version=(version)
    @data[:java][:java_version] = version if SUPPORTED_JAVA_VERSIONS.include? version
  end

  def maven_version=(version)
    @data[:java][:maven_version] = version if SUPPORTED_MAVEN_VERSIONS.include? version
  end

  def timeout=(seconds)
    @data[:timeout] = seconds.to_i
  end

  def write(file_name=nil)
    if @data[:tests].empty?
      puts "ERROR: Cannot write config file with empty :tests: section."
      return false
    else
      file = File.open(file_name || DEFAULT_CONFIG_NAME, "w")
      file.write(@data.to_yaml)
      file.close
    end
  end

end


class Jenkins2SolanoConverter

  def initialize(file_name, solano_config)
    @file_name = file_name
    @solano_config = solano_config
    @builders = {pre: [], post: []}
    @test_cmd = nil

    # Parse source file
    file = File.open(file_name)
    puts "#{file_name} is being parsed..."
    @doc = Nokogiri::XML(file)
    file.close    
  end

  def element(name)
    @doc.xpath("//#{name}").text
  end

  def disabled?
    element('disabled') == "true"
  end

  def sub_vars(text)
    text.gsub!('$WORKSPACE', '$TDDIUM_REPO_ROOT')
    text.gsub!('$JOB_NAME', '$TDDIUM_SESSION_ID')
    text.gsub!('$BUILD_NUMBER', '$TDDIUM_TEST_EXEC_ID')
    return text
  end

  def get_builder_file_name(type)
    # Create .solano dir if it does not exist
    solano_dir = Pathname.new(SOLANO_DIR_NAME)
    FileUtils::makedirs(solano_dir) if ! File.exists?(solano_dir)

    solano_dir.join(File.basename(@file_name)).sub_ext(".#{type}.sh").to_s
  end

  def save_builder(builder_file_name, commands)
    # Write pre/postbuilder content into the file
    file = File.open(builder_file_name, "w")
    commands.each do |command|
      file.puts sub_vars(command)
      file.puts
    end
    file.close
  end

  def extract_builders
    [:pre, :post].each do |type|
      commands = []
      @doc.xpath("//#{type}builders").children.each do |child|
        if child.name == 'hudson.tasks.Shell'
          cmd = child.elements.search('command').text
          commands << child.text.strip if ! cmd.empty?
        elsif child.name == 'hudson.tasks.Maven'
          cmd = child.elements.search('targets').text
          commands << "mvn #{cmd.strip}" if ! cmd.empty?
        end
      end

      if ! commands.empty?
        builder_file = get_builder_file_name(type)
        @builders[type] << "sh #{builder_file}"
        save_builder(builder_file, commands)
      end
    end
  end
  
  def extract_test_command
    # Extract path to pom.xml
    pom_xml = element 'rootPOM'

    # Parse out goals
    @test_cmd = element 'goals'
    return false if @test_cmd.empty?

    # Substitute Jenkins vars with the corresponding Solano vars.
    @test_cmd = sub_vars(@test_cmd)

    # Remove screenshots.baseurl (TODO: clarify after testing)
    @test_cmd.gsub!(/-Dscreenshots.baseurl=[^ ]* /, '')

    # Compose the final test command.
    @test_cmd = "mvn -B -f $TDDIUM_REPO_ROOT/#{pom_xml} #{@test_cmd}"
  end
  
  def convert
    return false if !extract_test_command
    extract_builders
    @solano_config.data[:tests] <<
      (@builders[:pre] + [@test_cmd] + @builders[:post]).join(' && ')
  end
  
  def run
    if disabled?
      puts "WARNING: Job in #{@file_name} is disabled. Conversion skipped."
      return false
    elsif @doc.children.first.name != "maven2-moduleset"
      puts "ERROR: Data in #{@file_name} does not contain maven2-moduleset structure."
      return false
    else
      if convert
        puts "File %s converted" % File.basename(@file_name)
			else
        puts "Could not convert file %s" % File.basename(@file_name)
      end
    end
  end

end # Jenkins2SolanoConverter


#
# --- Main part ---
#

# CLI
options = {}
OptionParser.new do |opts|
  opts.banner = "Usage: jenkins2solano.rb [options] [jenkins-configs]"
  opts.on("-j VERSION", "--java-version VERSION", SUPPORTED_JAVA_VERSIONS,
          "Java version") do |v|
    options[:java] = v
  end
  opts.on("-m VERSION", "--maven-version VERSION", SUPPORTED_MAVEN_VERSIONS,
          "Maven version, one of %s" % SUPPORTED_MAVEN_VERSIONS.to_s) do |v|
    options[:maven] = v
  end
  opts.on("-t SECONDS", "--timeout SECONDS", "Timeout of tests execution") do |t|
    options[:timeout] = t
  end
  opts.on("-v", "--[no-]verbose", "Verbosity level") do |v|
    options[:verbose] = v
  end
end.parse!

puts 'OPTIONS: '
puts options
files = ARGV.any? ? ARGV : Dir.glob('*.xml')
if files.empty?
  puts "ERROR: Please provide jenkins-configs argument."
  exit 1
end

# Instantiate SolanoConfig.
solano_config = SolanoConfig.new
solano_config.java_version = options[:java] if options.has_key?(:java)
solano_config.maven_version = options[:maven] if options.has_key?(:maven)
solano_config.timeout = options[:timeout]

# Convert files.
files.each do |file_name|
  Jenkins2SolanoConverter.new(file_name, solano_config).run
end

# Write Solano config.
solano_config.write
