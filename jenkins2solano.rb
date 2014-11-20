#!/bin/env ruby -w

require 'fileutils'
require 'nokogiri'
require 'pathname'

DEFAULT_CONFIG_NAME = 'solano.yml'
SOLANO_DIR_NAME = '.solano'

class Jenkins2SolanoConverter

  def initialize(file_name)
    puts "#{file_name} is being parsed..."

    @file_name = file_name
    @builders = {pre: [], post: []}
    @test_cmd = nil

    # Parse source file
    file = File.open(file_name)
    @doc = Nokogiri::XML(file)
    file.close    
  end

  def element(name)
    @doc.xpath("//#{name}").text
  end

  def disabled?
    element('disabled') == "true"
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
      file.write command
    end
    file.close
  end

  def extract_builders
    [:pre, :post].each do |type|
      commands = []
      @doc.xpath("//#{type}builders").children.each do |child|
        if child.name == 'hudson.tasks.Shell'
          commands << child.text
        elsif child.name == 'hudson.tasks.Maven'
          commands << "mvn #{child.text.strip}"
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

    # Parse out goals and replace Jenkins vars with the corresponding Solano vars.
    @test_cmd = element 'goals'
    @test_cmd.gsub!('$WORKSPACE', '$TDDIUM_REPO_ROOT')
    @test_cmd.gsub!('$JOB_NAME', '$TDDIUM_SESSION_ID')
    @test_cmd.gsub!('$BUILD_NUMBER', '$TDDIUM_TEST_EXEC_ID')
    @test_cmd.gsub!(/-Dscreenshots.baseurl=[^ ]* /, '')

    # Compose the final test command.
    @test_cmd = "mvn -B -f $TDDIUM_REPO_ROOT/#{pom_xml} #{@test_cmd}"
  end
  
  def convert
    extract_builders
    extract_test_command
    (@builders[:pre] + [@test_cmd] + @builders[:post]).join(' && ')
  end
  
  def run
    if disabled?
      puts "Job in #{@file_name} is disabled. Conversion skipped."
      return false
    else
      convert
    end
  end

end


def write_config(tests)
  file = File.open(DEFAULT_CONFIG_NAME, "w")
  file.close
end


tests = []
file_name = '/home/ilesnik/solano/aura-jenkins-configs/192patch_integration_firefox_config.xml'
converter = Jenkins2SolanoConverter.new(file_name)
tests << converter.run
#write_config(tests)
puts tests
